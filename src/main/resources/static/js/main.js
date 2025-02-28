document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    
    if (form) {
        form.addEventListener('submit', function(e) {
            const emailInput = document.getElementById('email');
            const emailValue = emailInput ? emailInput.value.trim() : '';
            
            if (!emailValue) {
                e.preventDefault();
                alert('Please enter an email address');
            }
        });
    }
});