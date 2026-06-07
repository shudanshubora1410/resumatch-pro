/** Job Analytics — Chart.js visualizations per job listing */
var JobAnalytics = {
    charts: [],
    jobId: null,
    init: function() {
        Utils.requireAuth();
        this.jobId = new URLSearchParams(window.location.search).get('jobId') || '1';
        this.loadData();
    },
    loadData: function() {
        var self = this;
        Skeleton.show('analyticsContent', 'card', 3);
        Api.get('/recruiter/jobs/' + this.jobId + '/analytics').then(function(r) {
            Skeleton.hide('analyticsContent');
            if (r.success) self.render(r.data);
        }).catch(function() { Skeleton.hide('analyticsContent'); });
    },
    render: function(data) {
        // Stats cards
        document.getElementById('statTotalApps').textContent = data.totalApplicants || 0;
        document.getElementById('statAvgScore').textContent = (data.avgScore ? data.avgScore.toFixed(1) : '0') + '%';
        document.getElementById('statShortlisted').textContent = data.shortlisted || 0;

        // Score distribution chart
        if (data.scoreDistribution) {
            this.renderBarChart('scoreDistChart', 'Score Distribution', data.scoreDistribution);
        }

        // Skills gap chart
        if (data.skillGaps) {
            this.renderSkillsGap(data.skillGaps);
        }
    },
    renderBarChart: function(canvasId, title, distribution) {
        var ctx = document.getElementById(canvasId);
        if (!ctx) return;
        this.charts.push(new Chart(ctx, {
            type: 'bar',
            data: {
                labels: Object.keys(distribution),
                datasets: [{
                    label: title,
                    data: Object.values(distribution),
                    backgroundColor: ['#EF4444', '#F59E0B', '#F59E0B', '#10B981', '#10B981', '#047857'],
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true, title: { display: true, text: 'Applicants' } } }
            }
        }));
    },
    renderSkillsGap: function(gaps) {
        var html = '';
        var sorted = Object.entries(gaps).sort(function(a, b) { return b[1] - a[1]; }).slice(0, 10);
        sorted.forEach(function(_a) {
            var skill = _a[0], count = _a[1];
            html += '<div class="skill-gap-bar"><span style="width:120px;font-size:0.85rem">' + skill + '</span><div class="gap-bar-track"><div class="gap-bar-fill" style="width:' + Math.min(100, count*10) + '%"></div></div><span class="gap-count">' + count + '</span></div>';
        });
        document.getElementById('skillsGap').innerHTML = html || '<p class="text-muted">No gap data available</p>';
    }
};
