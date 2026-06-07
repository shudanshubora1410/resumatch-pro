package com.resumatchpro.service;

import com.resumatchpro.exception.InvalidFileException;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import com.resumatchpro.utility.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock private ResumeRepository resumeRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileParserUtil fileParserUtil;
    @Mock private NLPProcessorUtil nlpProcessor;
    @Mock private FileStorageUtil fileStorageUtil;

    private ResumeService resumeService;

    @BeforeEach
    void setUp() {
        resumeService = new ResumeService(resumeRepository, userRepository, fileParserUtil, nlpProcessor, fileStorageUtil);
    }

    @Test
    void testUploadResume_validPdf_shouldSucceed() throws Exception {
        MultipartFile file = new MockMultipartFile("resume", "test.pdf", "application/pdf", "Sample resume content".getBytes());
        User user = User.builder().id(1L).email("test@test.com").build();
        FileParserUtil.ParsedResult parsed = new FileParserUtil.ParsedResult("Sample resume content", 3, 50, "PDF", "test.pdf");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fileParserUtil.parse(file)).thenReturn(parsed);
        when(fileStorageUtil.store(any(), anyString(), anyString())).thenReturn("uuid-test.pdf");
        when(nlpProcessor.process(anyString())).thenReturn(NLPProcessorUtil.ProcessedResume.empty());
        when(resumeRepository.save(any(Resume.class))).thenReturn(Resume.builder().id(1L).build());
        when(resumeRepository.countByUserIdAndIsActiveTrue(1L)).thenReturn(0L);

        Resume result = resumeService.uploadResume(1L, file);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testUploadResume_rateLimitExceeded_shouldThrow() throws Exception {
        MultipartFile file = new MockMultipartFile("resume", "test.pdf", "application/pdf", "Content".getBytes());
        when(resumeRepository.countByUserIdAndIsActiveTrue(1L)).thenReturn(10L);

        assertThrows(Exception.class, () -> resumeService.uploadResume(1L, file));
    }

    @Test
    void testUploadResume_invalidExtension_shouldReject() throws Exception {
        MultipartFile file = new MockMultipartFile("resume", "test.exe", "application/octet-stream", "malware".getBytes());
        when(fileParserUtil.parse(file)).thenThrow(new InvalidFileException("Invalid file type"));

        assertThrows(InvalidFileException.class, () -> resumeService.uploadResume(1L, file));
    }

    @Test
    void testQualityCheck_shouldReturnAnalysis() {
        Resume resume = Resume.builder().id(1L).extractedText("Skills: Java, Python\nExperience: 3 years").wordCount(10).build();
        when(resumeRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(resume));

        NLPProcessorUtil.ProcessedResume processed = mock(NLPProcessorUtil.ProcessedResume.class);
        when(processed.getWordCount()).thenReturn(10);
        when(processed.getDetectedSections()).thenReturn(new HashSet<>(Arrays.asList("SKILLS", "EXPERIENCE")));
        when(processed.getMissingSections()).thenReturn(Collections.singletonList("EDUCATION"));
        when(processed.getExtractedSkills()).thenReturn(new HashSet<>(Arrays.asList("java", "python")));
        when(processed.getContactInfo()).thenReturn(Map.of("email", true));
        when(processed.isAtsFriendly()).thenReturn(true);
        when(processed.getAchievements()).thenReturn(Collections.emptyList());
        when(nlpProcessor.process(anyString())).thenReturn(processed);

        Map<String, Object> result = resumeService.qualityCheck(1L, 1L);
        assertNotNull(result);
        assertEquals(1L, result.get("resumeId"));
        assertTrue(result.containsKey("qualityTier"));
    }
}
