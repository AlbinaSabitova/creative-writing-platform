function initBookshelf(topics) {
    const shelf = document.getElementById('cloud');
    if (!shelf) return;

    shelf.innerHTML = '';
    shelf.className = 'bookshelf';

    if (!topics?.length) return;

    const maxLength = Math.max(...topics.map(t => t.length));

    topics.forEach(topic => {
        const book = document.createElement('div');
        book.className = 'book';

        const height = 120 + (topic.length / maxLength) * 60;
        const width = 50 + (topic.length * 0.8);

        book.style.width = `${width}px`;

        const spine = document.createElement('div');
        spine.className = 'book-spine';
        spine.textContent = topic;
        spine.style.height = `${height}px`;
        spine.style.fontSize = `${12 + (topic.length / maxLength) * 8}px`;

        const top = document.createElement('div');
        top.className = 'book-top';

        book.append(spine, top);
        book.onclick = () => window.location.href = `/topic?name=${encodeURIComponent(topic)}`;
        shelf.appendChild(book);
    });
}