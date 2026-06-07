/** Analysis Report - Chart.js doughnut + all 6 categories + what-if */
var AnalysisReport = {
    chart: null,
    init: function() {
        Utils.requireAuth();
        var appId = new URLSearchParams(window.location.search).get('appId') || '1';
        this.load(appId);
    },
    load: function(appId) {
        var self = this;
        Skeleton.show('skeletonLoader', 'chart', 1);
        Api.get('/seeker/analysis/' + appId).then(function(r) {
            if (r.success) self.render(r.data);
            Skeleton.hide('skeletonLoader');
        }).catch(function() {
            Skeleton.hide('skeletonLoader');
        });
    },
    render: function(a) {
        document.getElementById('skeletonLoader').style.display = 'none';
        document.getElementById('analysisContent').style.display = 'block';
        if (!a) return;

        document.getElementById('aJobTitle').textContent = a.jobTitle;
        document.getElementById('aCompany').textContent = a.companyName || '';
        document.getElementById('aDate').textContent = Utils.formatDate(a.analysisDate);
        document.getElementById('aVersion').textContent = 'v' + (a.scoreVersion || 1);

        var ctx = document.getElementById('scoreDoughnut').getContext('2d');
        var sc = a.finalScore || 0;
        var color = sc >= 70 ? '#10B981' : sc >= 50 ? '#F59E0B' : '#EF4444';
        if (this.chart) this.chart.destroy();
        this.chart = new Chart(ctx, {
            type: 'doughnut',
            data: { datasets: [{ data: [sc, 100 - sc], backgroundColor: [color, '#E5E7EB'], borderWidth: 0, borderRadius: sc >= 100 ? 0 : 20 }]},
            options: { cutout: '75%', responsive: true, plugins: { legend: { display: false }, tooltip: { enabled: false } } }
        });
        document.getElementById('scoreNum').textContent = sc;
        document.getElementById('scoreNum').style.color = color;
        document.getElementById('scoreGrade').textContent = (a.grade||'') + ' - ' + (a.gradeLabel||'');
        document.getElementById('scoreGrade').className = 'badge fs-6 px-3 py-2 bg-' + (sc >= 70 ? 'success' : sc >= 50 ? 'warning' : 'danger');
        var atsEl = document.getElementById('atsStatus');
        atsEl.textContent = a.atsStatus || '';
        atsEl.className = 'badge px-3 py-1 ' + (sc >= 70 ? 'score-badge green' : sc >= 50 ? 'score-badge orange' : 'score-badge red');

        this.renderBreakdown(a);
        this.renderSkills(a);
        this.renderKeywords(a);
        this.renderExperience(a);
        this.renderAchievements(a);
        this.renderStructure(a);
        this.renderSuggestions(a);
        document.getElementById('overallFeedback').textContent = a.overallFeedback || '';
        if (a.missingKeywords || a.missingSkills) {
            WhatIfSimulator.init({ originalScore: sc, missingKeywords: a.missingKeywords||[], missingSkills: a.missingSkills||[], scoreBreakdown: a.scoreBreakdown||{} });
        }
    },
    renderBreakdown: function(a) {
        var html = '';
        var cats = a.scoreBreakdown || {};
        Object.keys(cats).forEach(function(k) {
            var cat = cats[k];
            var pct = cat.maxScore > 0 ? (cat.score / cat.maxScore * 100) : 0;
            var barColor = pct >= 70 ? 'bg-success' : pct >= 40 ? 'bg-warning' : 'bg-danger';
            html += '<div class="col-md-6"><div class="d-flex justify-content-between mb-1"><span class="fw-medium small">' + cat.label + '</span><span class="small">' + cat.score + '/' + cat.maxScore + '</span></div><div class="progress"><div class="progress-bar ' + barColor + '" style="width:' + pct + '%"></div></div></div>';
        });
        document.getElementById('scoreBreakdown').innerHTML = html;
    },
    renderSkills: function(a) {
        document.getElementById('matchedSkills').innerHTML = (a.matchedSkills||[]).map(function(s){return '<span class="skill-chip matched">'+s+'</span>'}).join(' ') || '<span class="text-muted small">None</span>';
        document.getElementById('missingSkills').innerHTML = (a.missingSkills||[]).map(function(s){return '<span class="skill-chip missing">'+s+' <i class="fa-solid fa-xmark"></i></span>'}).join(' ') || '<span class="text-success small">All matched!</span>';
    },
    renderKeywords: function(a) {
        document.getElementById('matchedKeywords').innerHTML = (a.matchedKeywords||[]).map(function(k){return '<span class="skill-chip matched">'+k+'</span>'}).join(' ') || '<span class="text-muted small">None</span>';
        document.getElementById('missingKeywords').innerHTML = (a.missingKeywords||[]).map(function(k){return '<span class="skill-chip missing">'+k+' <i class="fa-solid fa-xmark"></i></span>'}).join(' ') || '<span class="text-success small">All matched!</span>';
    },
    renderExperience: function(a) {
        var html = '<p class="small mb-1"><b>Action Verbs:</b> ' + (a.detectedVerbs||[]).map(function(v){return '<span class="badge bg-primary me-1">'+v+'</span>'}).join(' ') + '</p>';
        if (a.weakPhrases && a.weakPhrases.length) html += '<p class="small mb-1"><b>Weak:</b> ' + a.weakPhrases.map(function(w){return '<span class="badge bg-warning text-dark me-1">'+w+'</span>'}).join(' ') + '</p>';
        html += '<p class="small mb-1"><b>Experience:</b> ' + (a.estimatedYears||0) + ' years detected (Required: ' + (a.requiredYears||0) + ')</p>';
        document.getElementById('experienceAnalysis').innerHTML = html;
    },
    renderAchievements: function(a) {
        var html = '<p class="small mb-1"><b>Measurable:</b> ' + ((a.achievements||[]).length) + ' found</p>';
        (a.achievements||[]).forEach(function(ach){ html += '<p class="small mb-1"><i class="fa-solid fa-check-circle text-success me-1"></i>'+ach+'</p>'; });
        document.getElementById('achievementAnalysis').innerHTML = html;
    },
    renderStructure: function(a) {
        var secs = a.detectedSections || [], mis = a.missingSections || [], ci = a.contactInfo || {};
        var html = '<div class="col-md-6"><p class="small fw-bold mb-2">Detected:</p>' + secs.map(function(s){return '<span class="badge bg-success me-1 mb-1">'+s+' ✓</span>'}).join(' ') + '</div>';
        html += '<div class="col-md-6"><p class="small fw-bold mb-2">Missing:</p>' + (mis.length ? mis.map(function(s){return '<span class="badge bg-danger me-1 mb-1">'+s+' ✗</span>'}).join(' ') : '<span class="text-success small">None</span>') + '</div>';
        html += '<div class="col-12 mt-2"><p class="small"><b>Contact:</b> ';
        Object.keys(ci).forEach(function(k){ html += '<span class="badge '+(ci[k]?'bg-success':'bg-danger')+' me-1">'+k+(ci[k]?' ✓':' ✗')+'</span>'; });
        html += ' · <b>ATS:</b> <span class="badge '+(a.atsFriendly?'bg-success':'bg-danger')+'">'+(a.atsFriendly?'Yes':'No')+'</span> · <b>Words:</b> '+(a.resumeWordCount||0)+'</p></div>';
        document.getElementById('structureAnalysis').innerHTML = html;
    },
    renderSuggestions: function(a) {
        var html = '';
        (a.suggestions||[]).forEach(function(s) {
            var cls = s.impact === 'HIGH' ? 'high' : s.impact === 'MEDIUM' ? 'medium' : 'low';
            html += '<div class="improvement-card '+cls+'"><div class="d-flex justify-content-between"><div><span class="badge bg-dark me-2">#'+s.priority+'</span><span class="fw-medium">'+s.area+'</span></div><span class="badge '+(s.impact==='HIGH'?'bg-danger':s.impact==='MEDIUM'?'bg-warning text-dark':'bg-success')+'">'+s.impact+' · +'+s.estimatedScoreGain+' pts</span></div><p class="small mt-2 mb-0">'+s.suggestion+'</p></div>';
        });
        document.getElementById('improvementSuggestions').innerHTML = html;
    }
};
