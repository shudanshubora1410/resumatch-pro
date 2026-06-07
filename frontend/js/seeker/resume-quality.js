/** Standalone Resume Quality Check */
var ResumeQuality = {
    init: function() {
        Utils.requireAuth();
        this.loadResumes();
    },
    loadResumes: function() {
        var self = this;
        Api.get('/seeker/resume/all?page=0&size=50').then(function(r) {
            if (r.success && r.data.content) {
                var options = '<option value="">-- Choose a resume --</option>';
                r.data.content.forEach(function(resume) {
                    options += '<option value="' + resume.id + '">' + resume.originalFilename + ' (' + Utils.formatDate(resume.uploadDate) + ')</option>';
                });
                document.getElementById('resumeSelect').innerHTML = options;
            }
            document.getElementById('checkBtn').addEventListener('click', function() {
                var rid = document.getElementById('resumeSelect').value;
                if (!rid) { Toast.show('Select a resume', 'warning'); return; }
                self.runCheck(rid);
            });
        }).catch(function() {});
    },
    runCheck: function(resumeId) {
        var self = this;
        document.getElementById('qualityResult').innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary mb-2"></div><p class="text-muted">Analyzing resume quality...</p></div>';
        Api.get('/seeker/resume/' + resumeId + '/quality-check').then(function(r) {
            if (r.success) self.render(r.data);
            else self.showError(r.message || 'Check failed');
        }).catch(function() { self.showError('Network error'); });
    },
    render: function(data) {
        var tier = data.qualityTier || 'UNKNOWN';
        var tierColor = tier === 'EXCELLENT' ? 'success' : tier === 'GOOD' ? 'primary' : tier === 'FAIR' ? 'warning' : 'danger';
        var tierEmoji = tier === 'EXCELLENT' ? '🏆' : tier === 'GOOD' ? '👍' : tier === 'FAIR' ? '📝' : '⚠️';

        var html = '<div class="text-center mb-4"><div style="font-size:3rem">' + tierEmoji + '</div>' +
        '<h4 class="fw-bold text-' + tierColor + '">' + tier.replace('_',' ') + '</h4></div>';

        html += '<div class="row g-3">' +
        '<div class="col-md-6"><div class="card"><div class="card-body"><h6 class="fw-bold mb-3">Sections</h6>' +
        '<p><b>Detected:</b> ' + (data.detectedSections||[]).map(function(s){return '<span class="badge bg-success me-1 mb-1">'+s+' ✓</span>'}).join(' ') + '</p>' +
        '<p><b>Missing:</b> ' + ((data.missingSections||[]).length ? data.missingSections.map(function(s){return '<span class="badge bg-danger me-1 mb-1">'+s+' ✗</span>'}).join(' ') : '<span class="text-success">None</span>') + '</p>' +
        '</div></div></div>' +

        '<div class="col-md-6"><div class="card"><div class="card-body"><h6 class="fw-bold mb-3">Contact Info</h6>' +
        '<ul class="list-unstyled mb-0">' +
        '<li><i class="fa-solid ' + (data.contactInfo && data.contactInfo.email ? 'fa-check-circle text-success' : 'fa-xmark-circle text-danger') + ' me-2"></i>Email</li>' +
        '<li><i class="fa-solid ' + (data.contactInfo && data.contactInfo.phone ? 'fa-check-circle text-success' : 'fa-xmark-circle text-danger') + ' me-2"></i>Phone</li>' +
        '<li><i class="fa-solid ' + (data.contactInfo && data.contactInfo.linkedin ? 'fa-check-circle text-success' : 'fa-xmark-circle text-danger') + ' me-2"></i>LinkedIn</li>' +
        '<li><i class="fa-solid ' + (data.contactInfo && data.contactInfo.github ? 'fa-check-circle text-success' : 'fa-xmark-circle text-danger') + ' me-2"></i>GitHub</li></ul>' +
        '</div></div></div>' +

        '<div class="col-md-6"><div class="card"><div class="card-body"><h6 class="fw-bold mb-3">Stats</h6>' +
        '<p><b>Word Count:</b> ' + (data.wordCount||0) + ' ' + ((data.wordCount||0) < 300 ? '<span class="badge bg-warning text-dark">Low</span>' : '<span class="badge bg-success">Good</span>') + '</p>' +
        '<p><b>Skills Found:</b> ' + (data.skillCount||0) + ' ' + ((data.skillCount||0) < 5 ? '<span class="badge bg-danger">Too Few</span>' : data.skillCount < 15 ? '<span class="badge bg-warning text-dark">Decent</span>' : '<span class="badge bg-success">Strong</span>') + '</p>' +
        '<p><b>Estimated Exp:</b> ' + (data.estimatedExperienceYears||0) + ' years</p></div></div></div>' +

        '<div class="col-md-6"><div class="card"><div class="card-body"><h6 class="fw-bold mb-3">ATS Score</h6>' +
        '<p><b>ATS Friendly:</b> <span class="badge ' + (data.atsFriendly ? 'bg-success' : 'bg-danger') + '">' + (data.atsFriendly ? 'Yes ✓' : 'No — fix formatting') + '</span></p>' +
        '<p><b>Has Awards:</b> <span class="badge ' + (data.hasAwards ? 'bg-success' : 'bg-secondary') + '">' + (data.hasAwards ? 'Yes' : 'Not found') + '</span></p>' +
        '<p><b>Has Achievements:</b> <span class="badge ' + ((data.achievementsCount||0) > 0 ? 'bg-success' : 'bg-warning text-dark') + '">' + (data.achievementsCount||0) + ' found</span></p>' +
        '</div></div></div></div>';

        if (data.extractedSkills && data.extractedSkills.length > 0) {
            html += '<div class="card mt-3"><div class="card-body"><h6 class="fw-bold mb-2">Extracted Skills (' + data.extractedSkills.length + ')</h6>' +
            data.extractedSkills.map(function(s){return '<span class="skill-chip neutral">'+s+'</span>'}).join(' ') + '</div></div>';
        }

        document.getElementById('qualityResult').innerHTML = html;
    },
    showError: function(msg) {
        document.getElementById('qualityResult').innerHTML = '<div class="alert alert-warning">' + msg + '</div>';
    }
};
