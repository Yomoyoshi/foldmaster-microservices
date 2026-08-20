const API_BASE = '/api';

function getToken() {
    try {
        const user = getCurrentUser();
        return user ? user.token : null;
    } catch (e) {
        return null;
    }
}

async function fetchJSON(endpoint) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'GET',
        headers: headers
    });
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    const json = await response.json();
    // Извлекаем data из ApiResponse
    if (json && typeof json === 'object' && 'data' in json) {
        return json.data;
    }
    return json;
}

async function postJSON(endpoint, data) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(data)
    });
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    const json = await response.json();
    if (json && typeof json === 'object' && 'data' in json) {
        return json.data;
    }
    return json;
}

function openModal(imageUrl) {
    let modal = document.getElementById('imageModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'imageModal';
        modal.className = 'modal';
        modal.innerHTML = `
            <span class="close-modal">&times;</span>
            <img class="modal-img" src="" alt="Увеличенное фото">
        `;
        document.body.appendChild(modal);
        const closeSpan = modal.querySelector('.close-modal');
        closeSpan.onclick = () => {
            modal.style.display = 'none';
        };
        modal.onclick = (e) => {
            if (e.target === modal) modal.style.display = 'none';
        };
    }
    const modalImg = modal.querySelector('.modal-img');
    modalImg.src = imageUrl;
    modal.style.display = 'flex';
}