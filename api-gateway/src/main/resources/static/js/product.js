// product.js – детальная страница товара

let currentProductId = null;
let currentImages = [];

function updateBreadcrumbs(productName) {
    const container = document.getElementById('breadcrumbs');
    if (!container) return;
    const escapedName = escapeHtml(String(productName));
    container.innerHTML = `
        <a href="index.html">Главная</a>
        <span>/</span>
        <a href="catalog.html">Каталог</a>
        <span>/</span>
        <span class="current">${escapedName}</span>
    `;
}

async function loadProduct() {
    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get('id');
    const container = document.getElementById('productDetail');

    if (!productId) {
        container.innerHTML = '<p style="color:red;">Товар не указан.</p>';
        return;
    }
    currentProductId = productId;

    const hiddenProductId = document.getElementById('reviewProductId');
    if (hiddenProductId) hiddenProductId.value = productId;

    try {
        const product = await fetchJSON(`/products/${productId}`);
        if (!product) throw new Error('Товар не найден');

        updateBreadcrumbs(product.name);
        currentImages = product.images || [];

        let galleryHtml = '';
        if (currentImages.length > 0) {
            const mainImageSrc = currentImages[0];
            galleryHtml = `
                <div class="product-gallery">
                    <div class="main-image-container">
                        <img id="mainProductImage" src="${escapeHtml(String(mainImageSrc))}" alt="Главное фото" class="main-product-image">
                    </div>
                    <div class="thumbnails-wrapper">
                        <div class="thumbnails-scroll">
                            ${currentImages.map((img, idx) => `
                                <img src="${escapeHtml(String(img))}" alt="Миниатюра ${idx+1}"
                                     class="thumbnail ${idx === 0 ? 'active-thumbnail' : ''}"
                                     data-index="${idx}">
                            `).join('')}
                        </div>
                    </div>
                </div>
            `;
        } else {
            galleryHtml = `<p>Фотографии отсутствуют</p>`;
        }

        let specsHtml = '';
        if (product.specs && Object.keys(product.specs).length) {
            specsHtml = `
                <div class="specs-list">
                    <h3>📋 Характеристики</h3>
                    <div class="specs-grid-two-columns">
                        ${Object.entries(product.specs).map(([key, val]) => `
                            <div class="spec-item">
                                <div class="spec-key">${escapeHtml(String(key))}</div>
                                <div class="spec-value">${escapeHtml(String(val))}</div>
                            </div>
                        `).join('')}
                    </div>
                </div>
            `;
        }

        const fullHtml = `
            <h1>${escapeHtml(String(product.name))}</h1>
            <div class="product-price" style="font-size:2rem; margin: 1rem 0;">${escapeHtml(String(product.price))} рублей</div>
            <p>${escapeHtml(String(product.description || ''))}</p>
            ${galleryHtml}
            ${specsHtml}
        `;
        container.innerHTML = fullHtml;

        attachThumbnailHandlers();

        const mainImg = document.getElementById('mainProductImage');
        if (mainImg) {
            mainImg.addEventListener('click', () => {
                if (typeof openModal === 'function') openModal(mainImg.src);
            });
        }

        await loadAverageRating(productId);
        await loadReviews(productId);
        updateAuthUI();
    } catch (err) {
        console.error('Ошибка загрузки товара:', err);
        container.innerHTML = '<p style="color:red;">Не удалось загрузить информацию о товаре.</p>';
    }
}

function attachThumbnailHandlers() {
    const thumbnails = document.querySelectorAll('.thumbnail');
    const mainImg = document.getElementById('mainProductImage');
    if (!mainImg) return;
    thumbnails.forEach(thumb => {
        thumb.removeEventListener('click', thumbnailClickHandler);
        thumb.addEventListener('click', thumbnailClickHandler);
    });
}

function thumbnailClickHandler(e) {
    const thumb = e.currentTarget;
    const newSrc = thumb.src;
    const mainImg = document.getElementById('mainProductImage');
    if (mainImg) {
        mainImg.src = newSrc;
        document.querySelectorAll('.thumbnail').forEach(t => t.classList.remove('active-thumbnail'));
        thumb.classList.add('active-thumbnail');
    }
}

async function loadAverageRating(productId) {
    const block = document.getElementById('averageRatingBlock');
    if (!block) return;
    try {
        const data = await fetchJSON(`/reviews/product/${productId}/average-rating`);
        const avg = data.averageRating;
        const total = data.totalReviews;
        if (total === 0) {
            block.innerHTML = '<p>⭐ Нет оценок</p>';
        } else {
            const fullStars = Math.floor(avg);
            const halfStar = (avg % 1) >= 0.5 ? 1 : 0;
            const emptyStars = 5 - fullStars - halfStar;
            let starsHtml = '★'.repeat(fullStars);
            if (halfStar) starsHtml += '½';
            starsHtml += '☆'.repeat(emptyStars);
            block.innerHTML = `
                <div class="average-rating">
                    <div class="rating-stars">${starsHtml}</div>
                    <div class="rating-value">${avg} / 5</div>
                    <div class="rating-count">(${total} ${getDeclension(total, 'отзыв', 'отзыва', 'отзывов')})</div>
                </div>
            `;
        }
    } catch (err) {
        console.error('Ошибка загрузки среднего рейтинга:', err);
        block.innerHTML = '<p>⭐ Нет данных</p>';
    }
}

function getDeclension(number, one, two, five) {
    let n = Math.abs(number);
    n %= 100;
    if (n >= 5 && n <= 20) return five;
    n %= 10;
    if (n === 1) return one;
    if (n >= 2 && n <= 4) return two;
    return five;
}

async function loadReviews(productId) {
    const reviewsContainer = document.getElementById('reviewsList');
    if (!reviewsContainer) return;
    try {
        const reviews = await fetchJSON(`/reviews/product/${productId}`);
        if (!reviews.length) {
            reviewsContainer.innerHTML = '<p>Пока нет отзывов. Будьте первым!</p>';
            return;
        }
        reviewsContainer.innerHTML = reviews.map(rev => `
            <div class="review-item">
                <div class="review-header">
                    <strong>${escapeHtml(String(rev.authorName || rev.author || 'Аноним'))}</strong>
                    <span class="review-rating">${'★'.repeat(rev.rating)}${'☆'.repeat(5 - rev.rating)}</span>
                    <span class="review-date">${new Date(rev.createdAt).toLocaleDateString()}</span>
                </div>
                <div class="review-text">${escapeHtml(String(rev.comment))}</div>
            </div>
        `).join('');
    } catch (err) {
        console.error('Ошибка загрузки отзывов:', err);
        reviewsContainer.innerHTML = '<p>Не удалось загрузить отзывы.</p>';
    }
}

function updateAuthUI() {
    // функция остаётся, но пока не используется
}

async function checkIfUserCanReview() {
    // заглушка
}

async function submitReview(event) {
    event.preventDefault(); // Отключаем стандартную отправку формы

    const user = getCurrentUser();
    if (!user) {
        alert('Для отправки отзыва необходимо войти.');
        showAuthModal();
        return;
    }

    const productId = currentProductId;
    if (!productId) {
        alert('Товар не указан.');
        return;
    }

    const rating = parseInt(document.getElementById('reviewRating').value);
    const text = document.getElementById('reviewText').value.trim();

    if (!rating || !text) {
        alert('Заполните все поля.');
        return;
    }

    const reviewData = {
        productId: productId,
        rating: rating,
        text: text,
        userId: user.id,
        authorName: user.username
    };

    try {
        const response = await fetch('/api/reviews', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${user.token}`
            },
            body: JSON.stringify(reviewData)
        });

        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Ошибка отправки отзыва');
        }

        alert('Спасибо! Ваш отзыв добавлен.');
        document.getElementById('reviewForm').reset();
        // Перезагружаем отзывы и средний рейтинг
        await loadReviews(productId);
        await loadAverageRating(productId);
    } catch (err) {
        console.error('Ошибка отправки отзыва:', err);
        alert('Не удалось отправить отзыв: ' + err.message);
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

// Инициализация после загрузки DOM
document.addEventListener('DOMContentLoaded', () => {
    loadProduct();
    if (typeof updateNavAuth === 'function') updateNavAuth();
    if (typeof initAuthModal === 'function') initAuthModal();
    const form = document.getElementById('reviewForm');
    if (form) {
        form.removeEventListener('submit', submitReview);
        form.addEventListener('submit', submitReview);
    }
});