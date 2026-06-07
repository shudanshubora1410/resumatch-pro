/** Admin Dashboard */
var AdminDashboard = {
    init: function() {
        Api.get('/admin/analytics/overview').then(function(r) {
            if (r.success) {
                var d = r.data;
                document.getElementById('statUsers').textContent = d.totalUsers || 0;
                document.getElementById('statSeekers').textContent = d.totalSeekers || 0;
                document.getElementById('statRecruiters').textContent = d.totalRecruiters || 0;
                document.getElementById('statJobs').textContent = d.activeJobs || 0;
            }
        }).catch(function(){});
    }
};
