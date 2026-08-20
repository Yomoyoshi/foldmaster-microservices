// contacts.js – чат поддержки

const chatMessages = document.getElementById('chatMessages');
const messageInput = document.getElementById('messageInput');
const sendBtn = document.getElementById('sendMessageBtn');
const authRequiredMsg = document.getElementById('authRequiredMsg');
const chatArea = document.getElementById('chatArea');

// Загрузка сообщений пользователя
async function loadMessages() {
    const user = getCurrentUser();
    if (!user) {
        authRequiredMsg.style.display = 'block';
        chatArea.style.display = 'none';
        return;
    }

    authRequiredMsg.style.display = 'none';
    chatArea.style.display = 'block';

    try {
        const response = await fetch(`/api/contact/user/${user.id}`, {
            headers: {
                'Authorization': `Bearer ${user.token}`
            }
        });
        if (!response.ok) {
            throw new Error('Ошибка загрузки сообщений');
        }
        const result = await response.json();
        const messages = result.data || [];
        renderMessages(messages);
    } catch (err) {
        console.error('Ошибка загрузки сообщений:', err);
        chatMessages.innerHTML = '<p style="color: red;">Не удалось загрузить сообщения.</p>';
    }
}

// Рендер сообщений
function renderMessages(messages) {
    if (!messages.length) {
        chatMessages.innerHTML = '<div style="text-align: center; color: #94a3b8;">У вас пока нет сообщений. Напишите нам!</div>';
        return;
    }
    chatMessages.innerHTML = messages.map(msg => `
        <div class="message-item own">
            <div class="meta">
                <span>${escapeHtml(msg.name || 'Вы')}</span>
                <span>${new Date(msg.createdAt).toLocaleString()}</span>
            </div>
            <div class="text">${escapeHtml(msg.text)}</div>
        </div>
    `).join('');
    // Прокрутка вниз
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// Отправка нового сообщения
async function sendMessage() {
    const user = getCurrentUser();
    if (!user) {
        showAuthModal();
        return;
    }

    const text = messageInput.value.trim();
    if (!text) {
        alert('Введите текст сообщения.');
        return;
    }

    const messageData = {
        userId: user.id,
        name: user.username,
        email: user.email || '', // если есть, иначе пустая строка
        text: text
    };

    try {
        const response = await fetch('/api/contact', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${user.token}`
            },
            body: JSON.stringify(messageData)
        });

        if (response.status === 429) {
            const err = await response.json();
            alert(err.error || 'Превышен лимит сообщений (5 в сутки)');
            return;
        }
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.error || 'Ошибка отправки');
        }

        // Очистить поле и перезагрузить сообщения
        messageInput.value = '';
        await loadMessages();
        alert('Сообщение отправлено!');
    } catch (err) {
        console.error('Ошибка отправки:', err);
        alert('Не удалось отправить сообщение: ' + err.message);
    }
}

// Вспомогательная функция для экранирования HTML
function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

// Обработчики событий
document.addEventListener('DOMContentLoaded', () => {
    if (typeof updateNavAuth === 'function') updateNavAuth();
    if (typeof initAuthModal === 'function') initAuthModal();

    loadMessages();

    if (sendBtn) {
        sendBtn.addEventListener('click', sendMessage);
    }
    if (messageInput) {
        messageInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });
    }
});