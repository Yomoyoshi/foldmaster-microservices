// admin-products.js – CRUD для товаров (только для ADMIN)

document.addEventListener('DOMContentLoaded', async () => {
    const user = getCurrentUser();
    const authCheck = document.getElementById('authCheck');
    const formContainer = document.getElementById('productForm');
    const listContainer = document.getElementById('productList');

    if (!user || user.role !== 'ADMIN') {
        if (authCheck) authCheck.style.display = 'block';
        if (formContainer) formContainer.style.display = 'none';
        if (listContainer) listContainer.innerHTML = '<p>Доступ запрещён. Требуется роль ADMIN.</p>';
        return;
    }

    if (authCheck) authCheck.style.display = 'none';
    if (formContainer) formContainer.style.display = 'block';
    await loadProducts();

    document.getElementById('saveBtn').addEventListener('click', saveProduct);
    document.getElementById('cancelBtn').addEventListener('click', resetForm);

    // Обработчик загрузки изображений (вынесен сюда)
    document.getElementById('uploadImagesBtn').addEventListener('click', async function() {
        const user = getCurrentUser();
        if (!user) { alert('Войдите'); return; }
        const id = document.getElementById('editId').value;
        if (!id) { alert('Сначала создайте или выберите товар'); return; }
        const files = document.getElementById('productImages').files;
        if (!files.length) { alert('Выберите файлы'); return; }
        const formData = new FormData();
        for (let f of files) formData.append('images', f);
        try {
            const response = await fetch(`/api/products/${id}/images`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${user.token}` },
                body: formData
            });
            if (!response.ok) throw new Error('Ошибка загрузки');
            alert('Изображения загружены');
            await loadProducts();
        } catch (e) { alert(e.message); }
    });
});

async function loadProducts() {
    const list = document.getElementById('productList');
    try {
        const products = await fetchJSON('/products');
        console.log('Получены товары:', products);
        if (!products.length) {
            list.innerHTML = '<p>Нет товаров. Создайте первый!</p>';
            return;
        }
        list.innerHTML = products.map(p => `
            <div class="product-item" data-id="${p.id}">
                <h3>${escapeHtml(p.name)}</h3>
                <p><strong>Цена:</strong> ${p.price} ₽</p>
                <p><strong>В наличии:</strong> ${p.stockQuantity}</p>
                <p>${escapeHtml(p.description || '')}</p>
                <div class="actions">
                    <button class="btn-edit" onclick="editProduct(${p.id})">✏️ Редактировать</button>
                    <button class="btn-delete" onclick="deleteProduct(${p.id})">🗑️ Удалить</button>
                </div>
            </div>
        `).join('');
    } catch (err) {
        list.innerHTML = '<p style="color:red;">Ошибка загрузки товаров</p>';
        console.error(err);
    }
}

function editProduct(id) {
    const products = document.querySelectorAll('.product-item');
    products.forEach(el => {
        if (parseInt(el.dataset.id) === id) {
            const name = el.querySelector('h3').textContent;
            const price = parseFloat(el.querySelector('p strong').nextSibling.textContent);
            const stock = parseInt(el.querySelector('p:nth-child(3) strong').nextSibling.textContent);
            const desc = el.querySelector('p:last-of-type')?.textContent || '';
            document.getElementById('editId').value = id;
            document.getElementById('productName').value = name;
            document.getElementById('productPrice').value = price;
            document.getElementById('productStock').value = stock;
            document.getElementById('productDescription').value = desc;
            document.getElementById('formTitle').textContent = 'Редактировать товар';
            document.getElementById('saveBtn').textContent = 'Обновить';
        }
    });
}

function resetForm() {
    document.getElementById('editId').value = '';
    document.getElementById('productName').value = '';
    document.getElementById('productPrice').value = '';
    document.getElementById('productStock').value = '';
    document.getElementById('productDescription').value = '';
    document.getElementById('productImage').value = '';
    document.getElementById('productImages').value = '';
    document.getElementById('formTitle').textContent = 'Добавить товар';
    document.getElementById('saveBtn').textContent = 'Сохранить';
}

async function saveProduct() {
    const user = getCurrentUser();
    if (!user) {
        alert('Необходимо войти');
        return;
    }

    const id = document.getElementById('editId').value;
    const name = document.getElementById('productName').value.trim();
    const price = parseFloat(document.getElementById('productPrice').value);
    const stock = parseInt(document.getElementById('productStock').value);
    const description = document.getElementById('productDescription').value.trim();
    const imageUrl = document.getElementById('productImage').value.trim() || '';

    if (!name || isNaN(price) || isNaN(stock)) {
        alert('Заполните все обязательные поля (название, цена, количество)');
        return;
    }

    const productData = {
        name,
        price,
        stockQuantity: stock,
        description,
        imageUrl,
        active: true
    };

    try {
        const headers = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${user.token}`
        };
        let response;
        if (id) {
            response = await fetch(`/api/products/${id}`, {
                method: 'PUT',
                headers: headers,
                body: JSON.stringify(productData)
            });
        } else {
            response = await fetch('/api/products', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(productData)
            });
        }

        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Ошибка сохранения');
        }

        alert(id ? 'Товар обновлён' : 'Товар создан');
        resetForm();
        await loadProducts();
    } catch (err) {
        alert(err.message);
    }
}

async function deleteProduct(id) {
    const user = getCurrentUser();
    if (!user) return;
    if (!confirm('Удалить товар?')) return;
    try {
        const response = await fetch(`/api/products/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${user.token}`
            }
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Ошибка удаления');
        }
        alert('Товар удалён');
        await loadProducts();
    } catch (err) {
        alert(err.message);
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}