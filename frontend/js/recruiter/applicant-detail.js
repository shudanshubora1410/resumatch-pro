/** Applicant Detail View — full candidate profile with downloadable resume */
var ApplicantDetail = {
    appId: null,
    init: function() {
        Utils.requireAuth();
        this.appId = new URLSearchParams(window.location.search).get('appId');
        if (!this.appId) { document.getElementById('detailContent').innerHTML = '<div class="alert alert-warning">No application ID provided</div>'; return; }
        this.loadDetail();
    },
    loadDetail: function() {
        var self = this;
        document.getElementById('skeletonLoader').style.display = 'block';
        Api.get('/recruiter/applications/' + this.appId).then(function(r) {
            document.getElementById('skeletonLoader').style.display = 'none';
            if (r.success) self.render(r.data);
        }).catch(function() { self.showError('Failed to load application'); });
    },
    render: function(app) {
        document.getElementById('detailContent').style.display = 'block';
        document.getElementById('candName').textContent = app.jobSeeker ? app.jobSeeker.fullName : 'Candidate';
        document.getElementById('candEmail').textContent = app.jobSeeker ? app.jobSeeker.email : '';
        document.getElementById('candLocation').textContent = app.jobSeeker ? (app.jobSeeker.location || 'N/A') : 'N/A';
        document.getElementById('jobTitle').textContent = app.jobListing ? app.jobListing.jobTitle : '';
        document.getElementById('companyName').textContent = '';
        document.getElementById('appDate').textContent = Utils.formatDate(app.appliedAt);
        document.getElementById('appStatus').textContent = Utils.getStatusLabel(app.status);
        document.getElementById('appStatus').className = 'status-badge ' + Utils.getStatusClass(app.status);
        document.getElementById('resumeLink').href = '/api/recruiter/applications/' + app.id + '/resume';

        // Load analysis
        var self = this;
        Api.get('/recruiter/applications/' + this.appId + '/analysis').then(function(r) {
            if (r.success) self.renderAnalysis(r.data);
        }).catch(function() {});

        // Notes
        document.getElementById('saveNotes').addEventListener('click', function() {
            var notes = document.getElementById('recruiterNotes').value;
            Api.post('/recruiter/applications/' + self.appId + '/notes', { notes: notes }).then(function(r) {
                if (r.success) Toast.show('Notes saved', 'success');
            });
        });
    },
    renderAnalysis: function(a) {
        if (!a) return;
        var sc = a.finalScore || 0;
        var color = sc >= 70 ? '#10B981' : sc >= 50 ? '#F59E0B' : '#EF4444';
        document.getElementById('scoreDisplay').innerHTML = '<div style="width:120px;height:120px;border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto;background:' + (sc>=70?'#D1FAE5':sc>=50?'#FEF3C7':'#FEE2E2') + '"><span style="font-size:2rem;font-weight:800;color:'+color+'">' + sc + '</span></div>' +
        '<span class="badge ' + (sc>=70?'bg-success':sc>=50?'bg-warning text-dark':'bg-danger') + ' mt-2">' + (a.grade||'') + ' ' + (a.gradeLabel||'') + '</span>';

        // Score bars
        var categories = [
            { label: 'Keyword Match', score: a.keywordMatchScore||0, max: 30 },
            { label: 'Skill Relevance', score: a.skillRelevanceScore||0, max: 25 },
            { label: 'Experience', score: a.experienceQualityScore||0, max: 20 },
            { label: 'Achievements', score: a.achievementsScore||0, max: 15 },
            { label: 'Formatting', score: a.formattingScore||0, max: 10 },
            { label: 'Education', score: a.educationMatchScore||0, max: 5 }
        ];
        var barsHtml = '';
        categories.forEach(function(c) {
            var pct = c.max > 0 ? (c.score / c.max * 100) : 0;
            var barColor = pct >= 70 ? 'bg-success' : pct >= 40 ? 'bg-warning' : 'bg-danger';
            barsHtml += '<div class="col-6 mb-2"><small class="text-muted">' + c.label + '</small><div class="progress"><div class="progress-bar ' + barColor + '" style="width:' + pct + '%">' + c.score + '/' + c.max + '</div></div></div>';
        });
        document.getElementById('scoreBars').innerHTML = barsHtml;

        // Skills
        document.getElementById('matchedSkills').innerHTML = (a.matchedKeywords||'').split(',').filter(Boolean).map(function(s){return '<span class="skill-chip matched">'+s.trim()+'</span>'}).join('') || '<span class="text-muted">None</span>';
        document.getElementById('missingSkills').innerHTML = (a.missingKeywords||'').split(',').filter(Boolean).map(function(s){return '<span class="skill-chip missing">'+s.trim()+'</span>'}).join('') || '<span class="text-success">None</span>';
        document.getElementById('overallFeedback').textContent = a.overallFeedback || '';
    },
    showError: function(msg) {
        document.getElementById('skeletonLoader').style.display = 'none';
        document.getElementById('detailContent').innerHTML = '<div class="alert alert-danger">' + msg + '</div>';
    }
};
