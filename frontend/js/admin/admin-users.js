/** Admin User Management */
var AdminUsers = {
    currentPage: 0,
    init: function() { this.load(); },
    load: function() {
        var self = this;
        Api.get('/admin/users?page='+this.currentPage+'&size=20').then(function(r) {
            if (r.success) self.render(r.data.content, r.data);
        }).catch(function(){});
    },
    render: function(users, page) {
        var html = '<table class="table"><thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Actions</th></tr></thead><tbody>';
        (users||[]).forEach(function(u) {
            html += '<tr><td>'+u.id+'</td><td>'+u.fullName+'</td><td>'+u.email+'</td>'+
            '<td><span class="badge bg-info">'+u.role+'</span></td>'+
            '<td><span class="badge '+(u.isActive?'bg-success':'bg-danger')+'">'+(u.isActive?'Active':'Inactive')+'</span></td>'+
            '<td><button class="btn btn-sm btn-outline-secondary">Details</button> <button class="btn btn-sm btn-outline-danger">Ban</button></td></tr>';
        });
        html += '</tbody></table>';
        document.getElementById('usersContent').innerHTML = html;
    }
};
