function typeWriterInfinite(element, text, speed = 100) {
    let i = 0;

    function type() {
        if (i < text.length) {
            element.innerHTML += text.charAt(i);
            i++;
            setTimeout(type, speed);
        } else {
            // Reset and start over immediately
            i = 0;
            element.innerHTML = '';
            setTimeout(type, speed);
        }
    }
    type();
}

// Auto-initialize
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('[data-typewriter]').forEach(element => {
        const text = element.getAttribute('data-typewriter');
        const speed = element.getAttribute('data-speed') || 100;
        typeWriterInfinite(element, text, parseInt(speed));
    });
});