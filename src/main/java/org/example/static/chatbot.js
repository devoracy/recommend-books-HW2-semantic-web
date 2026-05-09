document.addEventListener("DOMContentLoaded", function () {
    createChatbot();
    loadStarters();
});

function createChatbot() {
    const chatbot = document.createElement("div");
    chatbot.id = "chatbot";

    chatbot.innerHTML = `
        <div id="chatbot-header">Book Assistant</div>
        <div id="chatbot-body">
            <div id="chat-starters"></div>
            <div id="chat-messages"></div>
            <div id="chat-input-area">
                <input id="chat-input" type="text" placeholder="Ask about books...">
                <button id="chat-send">Send</button>
            </div>
        </div>
    `;

    document.body.appendChild(chatbot);

    document.getElementById("chatbot-header").addEventListener("click", function () {
        const body = document.getElementById("chatbot-body");
        body.style.display = body.style.display === "none" ? "block" : "none";
    });

    document.getElementById("chat-send").addEventListener("click", sendMessage);

    document.getElementById("chat-input").addEventListener("keypress", function (event) {
        if (event.key === "Enter") {
            sendMessage();
        }
    });
}

function getPageContext() {
    return document.body.getAttribute("data-page-context") || "unknown";
}

function loadStarters() {
    const pageContext = getPageContext();

    fetch(`/api/chat/starters?pageContext=${encodeURIComponent(pageContext)}`)
        .then(response => response.json())
        .then(data => {
            const starterDiv = document.getElementById("chat-starters");
            starterDiv.innerHTML = "";

            data.starters.forEach(starter => {
                const button = document.createElement("button");
                button.className = "starter-button";
                button.textContent = starter;

                button.addEventListener("click", function () {
                    document.getElementById("chat-input").value = starter;
                    sendMessage();
                });

                starterDiv.appendChild(button);
            });
        });
}

function sendMessage() {
    const input = document.getElementById("chat-input");
    const message = input.value.trim();

    if (!message) {
        return;
    }

    addMessage(message, "user");
    input.value = "";

    fetch("/api/chat", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            message: message,
            pageContext: getPageContext()
        })
    })
        .then(response => response.json())
        .then(data => {
            addMessage(data.answer, "bot");
        })
        .catch(() => {
            addMessage("Error while contacting chatbot backend.", "bot");
        });
}

function addMessage(text, sender) {
    const messages = document.getElementById("chat-messages");

    const div = document.createElement("div");
    div.className = "chat-message " + (sender === "user" ? "chat-user" : "chat-bot");
    div.textContent = text;

    messages.appendChild(div);
    messages.scrollTop = messages.scrollHeight;
}