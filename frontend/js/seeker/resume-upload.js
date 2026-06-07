/** Resume Upload Handler */
var ResumeUpload = {
    selectedFile: null,
    init: function() {
        Utils.requireAuth();
        var self = this;
        var zone = document.getElementById('uploadZone');
        if (zone) {
            ['dragenter','dragover','dragleave','drop'].forEach(function(e) {
                zone.addEventListener(e, function(ev) { ev.preventDefault(); ev.stopPropagation(); });
            });
            ['dragenter','dragover'].forEach(function(e) {
                zone.addEventListener(e, function() { zone.classList.add('dragover'); });
            });
            ['dragleave','drop'].forEach(function(e) {
                zone.addEventListener(e, function() { zone.classList.remove('dragover'); });
            });
            zone.addEventListener('drop', function(e) {
                if (e.dataTransfer.files.length) self.handleFile(e.dataTransfer.files[0]);
            });
        }
    },
    handleFile: function(f) {
        if (!f) return;
        if (!f.name.match(/\.(pdf|docx)$/i)) { Toast.show('Only PDF/DOCX accepted','error'); return; }
        if (f.size > 5*1024*1024) { Toast.show('File exceeds 5MB','error'); return; }
        this.selectedFile = f;
        document.getElementById('fileInfo').classList.remove('d-none');
        document.getElementById('fileName').textContent = f.name;
        document.getElementById('fileSize').textContent = (f.size/1024).toFixed(1) + ' KB';
        document.getElementById('uploadBtn').disabled = false;
    },
    reset: function() {
        this.selectedFile = null;
        document.getElementById('fileInfo').classList.add('d-none');
        document.getElementById('uploadBtn').disabled = true;
        document.getElementById('resumeFile').value = '';
    },
    upload: function() {
        if (!this.selectedFile) return;
        var btn = document.getElementById('uploadBtn');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Uploading...';
        var fd = new FormData();
        fd.append('file', this.selectedFile);
        Api.upload('/seeker/resume/upload', fd).then(function(r) {
            if (r.success) {
                document.getElementById('uploadResult').innerHTML = '<div class="alert alert-success">Resume uploaded! <a href="analysis-report.html">View Analysis</a></div>';
                btn.innerHTML = '<i class="fa-solid fa-check me-2"></i>Uploaded';
                Toast.show('Resume uploaded! Analysis started...','success');
            } else {
                document.getElementById('uploadResult').innerHTML = '<div class="alert alert-danger">' + (r.message||'Upload failed') + '</div>';
                btn.disabled = false;
                btn.innerHTML = '<i class="fa-solid fa-upload me-2"></i>Upload & Analyze';
            }
        }).catch(function() {
            Toast.show('Network error','error');
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-upload me-2"></i>Upload & Analyze';
        });
    }
};
