/** My Applications - pipeline tracker */
var MyApplications = {
    currentFilter: 'ALL',
    currentPage: 0,
    apps: [],
    init: function() {
        Utils.requireAuth();
        this.loadApplications();
    },
    loadApplications: function() {
        var self = this;
        document.getElementById('skeletonLoader').style.display = 'block';
        Api.get('/seeker/applications?page=0&size=50').then(function(r) {
            document.getElementById('skeletonLoader').style.display = 'none';
            if (r.success) { self.apps = r.data.content || []; self.render(); }
        }).catch(function() {
            document.getElementById('skeletonLoader').style.display = 'none';
        });
    },
    filterApps: function(status) {
        this.currentFilter = status;
        document.querySelectorAll('#statusFilters button').forEach(function(b) { b.classList.remove('active'); });
        event.target.classList.add('active');
        this.render();
    },
    render: function() {
        var filtered = this.currentFilter === 'ALL' ? this.apps : this.apps.filter(function(a) { return a.status === this.currentFilter; }.bind(this));
        var html = '';
        if (!filtered.length) {
            html = '<div class="text-center py-5"><i class="fa-solid fa-inbox fa-3x text-muted mb-3 d-block"></i><p class="text-muted">No applications in this category</p><a href="browse-jobs.html" class="btn btn-primary btn-sm">Browse Jobs</a></div>';
        }
        filtered.forEach(function(a) {
            var sc = a.score || 0;
            var scoreCls = sc >= 70 ? 'score-badge green' : sc >= 50 ? 'score-badge orange' : 'score-badge red';
            var statusCls = Utils.getStatusClass(a.status);
            html += '<div class="app-row"><div class="row align-items-center">' +
            '<div class="col-md-4"><h6 class="fw-bold mb-1">' + a.jobTitle + '</h6><p class="small text-muted mb-0">' + a.companyName + '</p></div>' +
            '<div class="col-md-2"><span class="' + scoreCls + '">' + sc + '/100</span></div>' +
            '<div class="col-md-2"><span class="status-badge ' + statusCls + '">' + Utils.getStatusLabel(a.status) + '</span></div>' +
            '<div class="col-md-2"><small class="text-muted">' + Utils.formatDate(a.appliedAt) + '</small></div>' +
            '<div class="col-md-2 text-end"><a href="analysis-report.html?appId=' + a.id + '" class="btn btn-sm btn-outline-primary">View Report</a></div>' +
            '</div></div>';
        });
        document.getElementById('applicationsContent').innerHTML = html;
        Pagination.render('applicationsPagination', 0, Math.ceil(filtered.length / 20) || 1, function() {});
    }
};
