// catalog.js – загрузка и отображение каталога товаров

async function loadCatalog() {
    const container = document.getElementById('catalogContainer');
    if (!container) return;
    try {
        const products = await fetchJSON('/products');
        if (!products.length) {
            container.innerHTML = '<p style="text-align:center;">Нет товаров в каталоге.</p>';
            return;
        }

        container.innerHTML = products.map(product => `
            <div class="product-card">
                <div class="card-content">
                    <h3 class="product-title">${escapeHtml(String(product.name))}</h3>
                    <div class="product-price">${escapeHtml(String(product.price))} рублей</div>
                    <div class="gallery-scroll">
                        ${(product.images || []).map(img => `<img src="${escapeHtml(String(img))}" alt="фото кровати">`).join('')}
                    </div>
                    <p>${escapeHtml(String(product.description || ''))}</p>
                    <a href="product.html?id=${product.id}" class="btn">Подробнее →</a>
                </div>
            </div>
        `).join('');

        document.querySelectorAll('.gallery-scroll img').forEach(img => {
            img.addEventListener('click', (e) => {
                e.stopPropagation();
                if (typeof openModal === 'function') openModal(img.src);
            });
        });
    } catch (err) {
        console.error('Ошибка загрузки каталога:', err);
        container.innerHTML = '<p style="text-align:center; color:red;">Не удалось загрузить товары. Попробуйте позже.</p>';
    }
}

function escapeHtml(str) {
    if (str == null) return '';
    const s = String(str);
    return s.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

// Запуск загрузки каталога
loadCatalog();