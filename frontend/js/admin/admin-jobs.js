/** Admin Job Management */
var AdminJobs = {
    currentPage: 0,
    init: function() { this.load(); },
    load: function() {
        var self = this;
        Api.get('/admin/jobs?page='+this.currentPage+'&size=20').then(function(r) {
            if (r.success) self.render(r.data.content, r.data);
        }).catch(function(){});
    },
    render: function(jobs, page) {
        var html = '<table class="table"><thead><tr><th>ID</th><th>Title</th><th>Company</th><th>Status</th><th>Apps</th><th>Actions</th></tr></thead><tbody>';
        (jobs||[]).forEach(function(j) {
            html += '<tr><td>'+j.id+'</td><td>'+j.jobTitle+'</td><td>'+j.companyName+'</td>'+
            '<td><span class="badge '+(j.status==='ACTIVE'?'bg-success':'bg-secondary')+'">'+j.status+'</span></td>'+
            '<td>'+j.applicantCount+'</td>'+
            '<td><button class="btn btn-sm btn-outline-primary">View</button> <button class="btn btn-sm btn-outline-danger">Remove</button></td></tr>';
        });
        html += '</tbody></table>';
        document.getElementById('jobsContent').innerHTML = html;
    }
};
