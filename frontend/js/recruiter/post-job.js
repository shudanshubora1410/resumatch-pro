/** Multi-step Job Posting Wizard */
var PostJob = {
    step: 1, totalSteps: 4,
    init: function() {
        Utils.requireAuth();
        this.setupChipInputs();
        document.getElementById('jobDescription').addEventListener('input', function() {
            document.getElementById('descCount').textContent = this.value.length + ' characters';
        });
        document.getElementById('publishBtn').addEventListener('click', function() {
            var isDraft = document.getElementById('saveAsDraft').checked;
            this.disabled = true;
            this.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Publishing...';
            var self = this;
            setTimeout(function() {
                Toast.show(isDraft ? 'Saved as draft!' : 'Published!', 'success');
                setTimeout(function() { window.location.href = 'manage-jobs.html'; }, 1000);
            }, 800);
        });
        document.getElementById('isRemote').addEventListener('change', function() {
            document.getElementById('location').disabled = this.checked;
            if (this.checked) document.getElementById('location').value = 'Remote';
        });
    },
    setupChipInputs: function() {
        var inputs = { requiredSkills: 'requiredSkillsChips', atsKeywords: 'atsKeywordsChips' };
        var self = this;
        Object.keys(inputs).forEach(function(inputId) {
            var input = document.getElementById(inputId);
            var container = document.getElementById(inputs[inputId]);
            if (!input || !container) return;
            input.addEventListener('keydown', function(e) {
                if (e.key === 'Enter' || e.key === ',') {
                    e.preventDefault();
                    var val = input.value.replace(/,/g, '').trim();
                    if (val && !self.chipExists(container, val)) {
                        var chip = self.createChip(container, val);
                        container.appendChild(chip);
                    }
                    input.value = '';
                }
            });
        });
    },
    chipExists: function(container, val) {
        var exists = false;
        container.querySelectorAll('.chip').forEach(function(c) {
            if (c.textContent.replace('x','').trim().toLowerCase() === val.toLowerCase()) exists = true;
        });
        return exists;
    },
    createChip: function(container, val) {
        var chip = document.createElement('span');
        chip.className = 'chip';
        chip.innerHTML = val + ' <i class="fa-solid fa-xmark" style="cursor:pointer;margin-left:4px;font-size:0.7rem"></i>';
        chip.querySelector('i').addEventListener('click', function() { chip.remove(); });
        return chip;
    },
    changeStep: function(delta) {
        var ns = this.step + delta;
        if (ns < 1 || ns > this.totalSteps) return;
        document.getElementById('step' + this.step).classList.remove('active');
        document.getElementById('step' + ns).classList.add('active');
        for (var i = 1; i <= this.totalSteps; i++) {
            var dot = document.getElementById('step' + i + 'Dot');
            dot.classList.remove('active', 'done');
            if (i < ns) dot.classList.add('done');
            if (i === ns) dot.classList.add('active');
            if (i < this.totalSteps) document.getElementById('step' + i + 'Line').classList.toggle('done', i < ns);
        }
        this.step = ns;
        document.getElementById('prevBtn').disabled = this.step === 1;
        document.getElementById('nextBtn').style.display = this.step === this.totalSteps ? 'none' : '';
        if (this.step === this.totalSteps) this.updatePreview();
    },
    updatePreview: function() {
        document.getElementById('prevTitle').textContent = document.getElementById('jobTitle').value || 'Job Title';
        document.getElementById('prevMeta').textContent = (document.getElementById('industry').value||'') + ' · ' + (document.getElementById('jobType').value||'Full-time').replace('_',' ');
        document.getElementById('prevDesc').textContent = (document.getElementById('jobDescription').value||'').substring(0,200) + '...';
        document.getElementById('prevType').innerHTML = '<b>Type:</b> '+(document.getElementById('jobType').value||'').replace('_',' ');
        document.getElementById('prevLoc').innerHTML = '<b>Location:</b> '+(document.getElementById('isRemote').checked?'Remote':(document.getElementById('location').value||'N/A'));
        document.getElementById('prevExp').innerHTML = '<b>Exp:</b> '+(document.getElementById('minExp').value||0)+'+ years';
        document.getElementById('prevSalary').innerHTML = '<b>Salary:</b> '+(document.getElementById('salaryRange').value||'Not disclosed');
    }
};
