/**
 * Tag/Chip Input Component
 * Usage: ChipInput.init(containerId, { placeholder: 'Add skill...', onChange: function(chips){} })
 */
var ChipInput = {
    init: function(containerId, options) {
        options = options || {};
        var container = document.getElementById(containerId);
        if (!container) return;
        container.className = 'chip-container';
        container.setAttribute('tabindex', '0');
        var chips = [];
        var input = document.createElement('input');
        input.type = 'text';
        input.className = 'chip-input';
        input.placeholder = options.placeholder || 'Type and press Enter...';
        container.appendChild(input);
        function renderChips() {
            var existing = container.querySelectorAll('.chip');
            existing.forEach(function(c) { c.remove(); });
            chips.forEach(function(chip, idx) {
                var span = document.createElement('span');
                span.className = 'chip';
                if (options.chipClass) span.className += ' ' + options.chipClass;
                span.innerHTML = chip + ' <i class="fa-solid fa-xmark" data-idx="' + idx + '"></i>';
                span.querySelector('i').addEventListener('click', function(e) {
                    e.stopPropagation();
                    chips.splice(parseInt(this.dataset.idx), 1);
                    renderChips();
                    if (options.onChange) options.onChange(chips);
                });
                container.insertBefore(span, input);
            });
        }
        input.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' || e.key === ',') {
                e.preventDefault();
                var val = input.value.trim().replace(/,/g, '');
                if (val && chips.indexOf(val) === -1) {
                    chips.push(val);
                    renderChips();
                    if (options.onChange) options.onChange(chips);
                }
                input.value = '';
            }
            if (e.key === 'Backspace' && input.value === '' && chips.length > 0) {
                chips.pop();
                renderChips();
                if (options.onChange) options.onChange(chips);
            }
        });
        container.addEventListener('click', function() { input.focus(); });

        return {
            getChips: function() { return chips; },
            setChips: function(newChips) { chips = newChips; renderChips(); },
            clear: function() { chips = []; renderChips(); if (options.onChange) options.onChange(chips); }
        };
    }
};
