/** Bulk Resume Screening */
var BulkScreen = {
    selectedFiles: [],
    init: function() {
        Utils.requireAuth();
        var self = this;
        var zone = document.getElementById('uploadZone');
        if (zone) {
            ['dragenter','dragover','dragleave','drop'].forEach(function(e) {
                zone.addEventListener(e, function(ev) { ev.preventDefault(); ev.stopPropagation(); });
            });
            ['dragenter','dragover'].forEach(function(e) { zone.addEventListener(e, function() { zone.classList.add('dragover'); }); });
            ['dragleave','drop'].forEach(function(e) { zone.addEventListener(e, function() { zone.classList.remove('dragover'); }); });
            zone.addEventListener('drop', function(e) { self.handleFiles(e.dataTransfer.files); });
        }
    },
    handleFiles: function(files) {
        var self = this;
        Array.from(files).forEach(function(f) {
            if (!f.name.match(/\.(pdf|docx)$/i)) { Toast.show(f.name + ' not PDF/DOCX','error'); return; }
            if (f.size > 5*1024*1024) { Toast.show(f.name + ' exceeds 5MB','error'); return; }
            if (self.selectedFiles.length >= 50) { Toast.show('Max 50 files','warning'); return; }
            if (!self.selectedFiles.find(function(sf) { return sf.name === f.name; })) self.selectedFiles.push(f);
        });
        this.renderFilePreview();
    },
    removeFile: function(idx) { this.selectedFiles.splice(idx, 1); this.renderFilePreview(); },
    renderFilePreview: function() {
        var html = this.selectedFiles.map(function(f, i) {
            return '<span class="file-chip">'+f.name+' ('+(f.size/1024).toFixed(0)+'KB) <i class="fa-solid fa-xmark text-danger" style="cursor:pointer" onclick="BulkScreen.removeFile('+i+')"></i></span>';
        }).join('');
        document.getElementById('filePreview').innerHTML = html;
        document.getElementById('screenBtn').disabled = this.selectedFiles.length === 0 || !document.getElementById('targetJob').value;
    },
    startScreening: function() {
        document.getElementById('screenBtn').disabled = true;
        document.getElementById('screenBtn').innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Screening...';
        var self = this;
        setTimeout(function() {
            var html = self.selectedFiles.map(function(f, i) {
                var score = Math.floor(Math.random() * 60) + 30;
                var color = score >= 70 ? '#10B981' : score >= 50 ? '#F59E0B' : '#EF4444';
                return '<div class="result-row"><div class="d-flex justify-content-between align-items-center"><div><span class="fw-medium">'+(i+1)+'. '+f.name.replace(/\.(pdf|docx)$/i,'')+'</span></div><div><span style="font-weight:700;color:'+color+'">'+score+'/100</span></div><button class="btn btn-sm btn-outline-primary">Download</button></div></div>';
            }).join('');
            document.getElementById('resultsContainer').innerHTML = html;
            document.getElementById('screenBtn').innerHTML = '<i class="fa-solid fa-play me-2"></i>Start Screening';
            document.getElementById('screenBtn').disabled = false;
            Toast.show(self.selectedFiles.length + ' resumes screened!','success');
        }, 1500);
    }
};
