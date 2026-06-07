/** Resume Version Comparison — side-by-side ATS delta */
var ResumeCompare = {
    r1: null, r2: null,
    init: function() {
        Utils.requireAuth();
        this.loadResumeList();
    },
    loadResumeList: function() {
        var self = this;
        Api.get('/seeker/resume/all?page=0&size=50').then(function(r) {
            if (r.success && r.data.content) {
                var options = '<option value="">-- Select Resume --</option>';
                r.data.content.forEach(function(resume) {
                    options += '<option value="' + resume.id + '">' + resume.originalFilename + ' (' + Utils.formatDate(resume.uploadDate) + ')</option>';
                });
                document.getElementById('resume1Select').innerHTML = options;
                document.getElementById('resume2Select').innerHTML = options;
            }
            self.attachListeners();
        }).catch(function() { self.showError('Failed to load resumes'); });
    },
    attachListeners: function() {
        var self = this;
        document.getElementById('compareBtn').addEventListener('click', function() {
            var r1Id = document.getElementById('resume1Select').value;
            var r2Id = document.getElementById('resume2Select').value;
            if (!r1Id || !r2Id) { Toast.show('Select two resumes to compare', 'warning'); return; }
            if (r1Id === r2Id) { Toast.show('Select different resumes', 'warning'); return; }
            self.runComparison(r1Id, r2Id);
        });
    },
    runComparison: function(r1Id, r2Id) {
        var self = this;
        document.getElementById('compareResult').innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary mb-2"></div><p class="text-muted">Comparing...</p></div>';
        Api.get('/seeker/resume/compare?r1=' + r1Id + '&r2=' + r2Id).then(function(r) {
            if (r.success) self.render(r.data, r1Id, r2Id);
        }).catch(function() { self.showError('Comparison failed'); });
    },
    render: function(data, r1Id, r2Id) {
        var r1 = data.resume1, r2 = data.resume2;
        var skillsGained = data.skillsGained || [];
        var sectionsAdded = data.sectionsAdded || [];
        var wordDelta = (r2.wordCount || 0) - (r1.wordCount || 0);
        var skillDelta = (r2.skills ? r2.skills.length : 0) - (r1.skills ? r1.skills.length : 0);

        var html = '<div class="row g-3">' +
        '<div class="col-md-6"><div class="card"><div class="card-header bg-light"><h6 class="fw-bold mb-0">Version 1</h6><small class="text-muted">' + Utils.formatDate(r1.uploadDate) + '</small></div><div class="card-body">' +
        '<p><b>Words:</b> ' + (r1.wordCount||0) + '</p><p><b>Sections:</b> ' + (r1.sectionCount||0) + '</p>' +
        '<p><b>Skills:</b> ' + ((r1.skills||[]).length) + '</p>' +
        '<div>' + (r1.skills||[]).slice(0,10).map(function(s){return '<span class="skill-chip neutral">'+s+'</span>'}).join('') + '</div></div></div></div>' +

        '<div class="col-md-6"><div class="card"><div class="card-header bg-light"><h6 class="fw-bold mb-0">Version 2</h6><small class="text-muted">' + Utils.formatDate(r2.uploadDate) + '</small></div><div class="card-body">' +
        '<p><b>Words:</b> ' + (r2.wordCount||0) + ' <span class="badge ' + (wordDelta >= 0 ? 'bg-success' : 'bg-danger') + '">' + (wordDelta >= 0 ? '+' : '') + wordDelta + '</span></p>' +
        '<p><b>Sections:</b> ' + (r2.sectionCount||0) + ' <span class="badge ' + (sectionsAdded.length >= 0 ? 'bg-success' : 'bg-danger') + '">' + (sectionsAdded.length > 0 ? '+' + sectionsAdded.length : '') + '</span></p>' +
        '<p><b>Skills:</b> ' + ((r2.skills||[]).length) + ' <span class="badge ' + (skillDelta >= 0 ? 'bg-success' : 'bg-danger') + '">' + (skillDelta >= 0 ? '+' : '') + skillDelta + '</span></p>' +
        '<div>' + (r2.skills||[]).slice(0,10).map(function(s){return '<span class="skill-chip neutral">'+s+'</span>'}).join('') + '</div></div></div></div>' +
        '</div>';

        if (skillsGained.length > 0 || sectionsAdded.length > 0) {
            html += '<div class="card mt-3"><div class="card-header bg-success bg-opacity-10"><h6 class="fw-bold mb-0 text-success"><i class="fa-solid fa-arrow-up me-2"></i>Improvements in Version 2</h6></div><div class="card-body"><div class="row">';
            if (skillsGained.length > 0) html += '<div class="col-md-6"><p class="fw-medium small mb-2">New Skills Added:</p>' + skillsGained.map(function(s){return '<span class="skill-chip matched">'+s+'</span>'}).join('') + '</div>';
            if (sectionsAdded.length > 0) html += '<div class="col-md-6"><p class="fw-medium small mb-2">New Sections Added:</p>' + sectionsAdded.map(function(s){return '<span class="badge bg-success me-1">'+s+'</span>'}).join('') + '</div>';
            html += '</div></div></div>';
        }

        document.getElementById('compareResult').innerHTML = html;
    },
    showError: function(msg) {
        document.getElementById('compareResult').innerHTML = '<div class="alert alert-warning">' + msg + '</div>';
    }
};
