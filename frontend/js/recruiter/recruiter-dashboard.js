/** Recruiter Dashboard */
var RecruiterDashboard = {
    init: function() {
        Utils.requireAuth();
        var u = Utils.getUser();
        if (u) document.getElementById('userNameDisplay').textContent = u.fullName || 'Recruiter';
        this.loadStats();
    },
    loadStats: function() {
        Api.get('/recruiter/dashboard/stats').then(function(r) {
            if (r.success) {
                var d = r.data;
                document.getElementById('statJobs').textContent = d.totalActiveJobs || 0;
                document.getElementById('statApps').textContent = d.totalApplications || 0;
                document.getElementById('statShortlisted').textContent = d.shortlistedCount || 0;
                document.getElementById('statAvgScore').textContent = (d.averageAtsScore || 0) + '%';
                document.getElementById('statRejected').textContent = d.rejectedCount || 0;
                document.getElementById('statPending').textContent = d.pendingCount || 0;
            }
        }).catch(function(){});
    }
};
