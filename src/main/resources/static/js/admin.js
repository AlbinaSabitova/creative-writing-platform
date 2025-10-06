document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('editModal');
    const closeBtn = document.querySelector('.close');
    const editForm = document.getElementById('editForm');

    // Open modal when edit button is clicked
    document.querySelectorAll('.btn-edit').forEach(button => {
        button.addEventListener('click', function() {
            const textId = this.getAttribute('data-id');
            const title = this.getAttribute('data-title');
            const content = this.getAttribute('data-content');
            const topics = this.getAttribute('data-topics');

            // Fill the form with current values
            document.getElementById('editTextId').value = textId;
            document.getElementById('editTitle').value = title;
            document.getElementById('editContent').value = content;
            document.getElementById('editTopics').value = topics || '';

            // Show the modal
            modal.style.display = 'block';
        });
    });

    // Close modal
    closeBtn.addEventListener('click', function() {
        modal.style.display = 'none';
    });

    // Close modal if clicked outside
    window.addEventListener('click', function(event) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });

    // Handle form submission
    editForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const formData = new FormData(this);
        const textId = formData.get('id');

        fetch('/admin/texts/' + textId + '/edit', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (response.ok) {
                    modal.style.display = 'none';
                    location.reload(); // Reload to see changes
                } else {
                    alert('Error updating text');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error updating text');
            });
    });
});

////
// Add preview functionality
document.querySelectorAll('.btn-view').forEach(button => {
    button.addEventListener('click', function() {
        const textId = this.getAttribute('data-id');
        showTextPreview(textId);
    });
});

function showTextPreview(textId) {
    fetch('/admin/texts/' + textId + '/preview-data')
        .then(response => response.json())
        .then(text => {
            const previewContent = document.getElementById('previewContent');
            previewContent.innerHTML = `
                <h3>${escapeHtml(text.title)}</h3>
                <p class="meta">
                    By <strong>${escapeHtml(text.authorName)}</strong> | 
                    Submitted: ${new Date(text.publicationDate).toLocaleDateString()}
                </p>
                
                <div class="topics">
                    ${text.topics.map(topic =>
                `<span class="topic">#${escapeHtml(topic)}</span>`
            ).join(' ')}
                </div>
                
                ${text.imageUrl ? `
                    <div class="text-image">
                        <img src="${escapeHtml(text.imageUrl)}" alt="Text image">
                    </div>
                ` : ''}
                
                <div class="content-preview">
                    ${escapeHtml(text.content)}
                </div>
            `;

            document.getElementById('previewModal').style.display = 'block';
        })
        .catch(error => {
            console.error('Error loading preview:', error);
            alert('Error loading text preview');
        });
}

// Helper function to escape HTML
function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe
        .toString()
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Add close functionality for preview modal
document.querySelectorAll('.modal .close').forEach(closeBtn => {
    closeBtn.addEventListener('click', function() {
        this.closest('.modal').style.display = 'none';
    });
});