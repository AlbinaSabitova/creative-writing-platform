document.addEventListener('DOMContentLoaded', function() {
    // Flash message auto-dismiss
    const flashMessages = document.querySelectorAll('.flash-message');
    flashMessages.forEach(msg => {
        setTimeout(() => {
            msg.style.transition = 'opacity 0.5s';
            msg.style.opacity = '0';
            setTimeout(() => msg.remove(), 500);
        }, 3000);
    });

    const textContent = document.getElementById('content');
    if (textContent) {
        const counter = document.createElement('div');
        counter.className = 'char-counter';
        textContent.parentNode.insertBefore(counter, textContent.nextSibling);

        textContent.addEventListener('input', () => {
            const remaining = 20000 - textContent.value.length;
            counter.textContent = `${textContent.value.length}/20000 characters`;
            counter.style.color = remaining < 1000 ? '#e74c3c' : '#7f8c8d';
        });
    }
});

// Modal functionality
document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('registerModal');
    const showModalBtn = document.getElementById('showRegisterModal');
    const closeBtn = document.querySelector('.close');
    const form = document.getElementById('modalRegisterForm');

    // Show modal
    showModalBtn.addEventListener('click', function(e) {
        e.preventDefault();
        modal.style.display = 'block';
    });

    // Close modal
    closeBtn.addEventListener('click', function() {
        modal.style.display = 'none';
    });

    // Close when clicking outside
    window.addEventListener('click', function(e) {
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });

    // Handle form submission
    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        const formData = new FormData(form);
        const data = Object.fromEntries(formData.entries());

        try {
            const response = await fetch('/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (response.ok) {
                modal.style.display = 'none';
                window.location.href = '/';
            } else {
                alert('Registration failed: ' + result.error);
            }
        } catch (error) {
            alert('Registration error: ' + error.message);
        }
    });
});

function toggleText(bubble) {
    bubble.style.opacity = "0";

    setTimeout(() => {
        if (bubble.textContent.trim() === "Подсказка") {
            bubble.textContent = "Для регистрации нажмите на ключ";
            bubble.style.backgroundColor = "#fff3cd";
        } else {
            bubble.textContent = "Подсказка";
            bubble.style.backgroundColor = "#fff";
        }
        bubble.style.opacity = "1";
    }, 200);
}