// Microbot CommandBridge - Clean Frontend Implementation
class MicrobotUI {
    constructor() {
        this.ws = null;
        this.state = {
            playerData: null,
            bankItems: [],
            walkStatus: { status: 'idle', steps: 0, total: 0 },
            connectionStatus: 'disconnected'
        };
        
        this.elements = {
            status: document.getElementById('status'),
            playerInfo: document.getElementById('player-info'),
            bankItemSelect: document.getElementById('bank-item-select'),
            walkStatus: document.getElementById('walk-status'),
            map: null
        };
        
        this.presetLocations = [
            { name: 'Grand Exchange', x: 3165, y: 3487 },
            { name: 'Varrock East Bank', x: 3253, y: 3420 },
            { name: 'Falador East Bank', x: 3013, y: 3355 },
            { name: 'Edgeville Bank', x: 3094, y: 3492 }
        ];
        
        this.init();
    }

    init() {
        this.connectWebSocket();
        this.setupEventListeners();
        this.initMap();
        this.render();
    }

    connectWebSocket() {
        this.ws = new WebSocket('ws://127.0.0.1:8777');
        
        this.ws.onopen = () => {
            this.updateState({ connectionStatus: 'connected' });
            this.elements.status.textContent = 'Connected to Microbot CommandBridge!';
        };

        this.ws.onclose = () => {
            this.updateState({ connectionStatus: 'disconnected' });
            this.elements.status.textContent = 'WebSocket connection closed. Reconnecting...';
            setTimeout(() => this.connectWebSocket(), 3000);
        };

        this.ws.onerror = () => {
            this.updateState({ connectionStatus: 'error' });
            this.elements.status.textContent = 'WebSocket error occurred';
        };

        this.ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                this.handleMessage(data);
            } catch (err) {
                console.error('Error parsing message:', err);
            }
        };
    }

    handleMessage(data) {
        if (data.command === 'BankItems' && Array.isArray(data.items)) {
            this.updateState({ bankItems: data.items });
        } else if (data.username) {
            this.updateState({ playerData: data });
        } else if (data.walk_status) {
            this.updateState({ 
                walkStatus: {
                    status: data.walk_status,
                    steps: data.steps_taken || 0,
                    total: data.total_steps || 0
                }
            });
        }
        this.render();
    }

    updateState(newState) {
        this.state = Object.assign({}, this.state, newState);
    }

    sendCommand(command, data = {}) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(Object.assign({}, { command }, data)));
        }
    }

    setupEventListeners() {
        // Bank controls
        document.getElementById('sync-bank-btn').addEventListener('click', () => {
            this.sendCommand('syncBank');
        });

        document.getElementById('withdraw-bank-btn').addEventListener('click', () => {
            const selected = this.elements.bankItemSelect.value;
            if (selected) {
                this.sendCommand('withdrawItem', { itemId: selected });
            }
        });

        // Bank actions
        document.getElementById('open-bank-btn').addEventListener('click', () => {
            this.sendCommand('openBank');
        });

        document.getElementById('close-bank-btn').addEventListener('click', () => {
            this.sendCommand('closeBank');
        });

        document.getElementById('get-bank-items-btn').addEventListener('click', () => {
            this.sendCommand('getBankItems');
        });

        // Map actions
        document.getElementById('pick-destination-btn').addEventListener('click', () => {
            this.sendCommand('pickDestination');
        });

        document.getElementById('go-bank-btn').addEventListener('click', () => {
            this.sendCommand('goToBank');
        });

        // Preset locations
        document.getElementById('go-preset-btn').addEventListener('click', () => {
            const select = document.getElementById('preset-location-select');
            const selected = select.value;
            if (selected) {
                const location = this.presetLocations.find(l => 
                    l.name.toLowerCase().replace(/\s+/g, '_') === selected);
                if (location) {
                    this.sendCommand('walkTo', { x: location.x, y: location.y });
                }
            }
        });

        // Walk coordinates
        document.getElementById('walk-btn').addEventListener('click', () => {
            const x = parseInt(document.getElementById('x-coord').value);
            const y = parseInt(document.getElementById('y-coord').value);
            if (!isNaN(x) && !isNaN(y)) {
                this.sendCommand('walkTo', { x: x, y: y });
            }
        });

        document.getElementById('stop-btn').addEventListener('click', () => {
            this.sendCommand('stopWalk');
        });
    }

    initMap() {
        const map = L.map('map').setView([3234, 3200], 5);
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        let marker = null;
        
        map.on('click', (e) => {
            const lat = e.latlng.lat;
            const lng = e.latlng.lng;
            if (marker) {
                marker.remove();
            }
            marker = L.marker([lat, lng]).addTo(map);
            this.sendCommand('walkTo', { x: Math.round(lat), y: Math.round(lng) });
        });

        this.elements.map = map;
    }

    render() {
        this.renderPlayerInfo();
        this.renderBankItems();
        this.renderWalkStatus();
    }

    renderPlayerInfo() {
        const playerData = this.state.playerData;
        const container = this.elements.playerInfo;
        
        if (!playerData) {
            container.innerHTML = '<div class="loading">No player data available</div>';
            return;
        }

        container.innerHTML = `
            <div><strong>Username:</strong> ${playerData.username || 'Unknown'}</div>
            <div><strong>Location:</strong> ${playerData.location || 'Unknown'}</div>
            <div><strong>Health:</strong> ${playerData.health || 'Unknown'}</div>
            <div><strong>Inventory:</strong> ${playerData.inventory || 'Empty'}</div>
        `;
    }

    renderBankItems() {
        const bankItems = this.state.bankItems;
        const select = this.elements.bankItemSelect;
        
        if (!bankItems || bankItems.length === 0) {
            select.innerHTML = '<option value="">No items - Sync needed</option>';
            return;
        }

        select.innerHTML = bankItems.map(item => 
            '<option value="' + item.id + '">' + item.name + ' (x' + item.quantity + ')</option>'
        ).join('');
    }

    renderWalkStatus() {
        const walkStatus = this.state.walkStatus;
        const container = this.elements.walkStatus;
        
        container.innerHTML = `
            <div><strong>Status:</strong> ${walkStatus.status}</div>
            <div><strong>Steps:</strong> ${walkStatus.steps} / ${walkStatus.total}</div>
        `;
    }
}

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    new MicrobotUI();
});
class MicrobotUI {
    constructor() {
        this.ws = null;
        this.state = {
            playerData: null,
            bankItems: [],
            walkStatus: { status: 'idle', steps: 0, total: 0 },
            connectionStatus: 'disconnected'
        };
        
        this.elements = {
            status: document.getElementById('status'),
            playerInfo: document.getElementById('player-info'),
            bankItemSelect: document.getElementById('bank-item-select'),
            walkStatus: document.getElementById('walk-status'),
            map: null
        };
        
        this.presetLocations = [
            { name: 'Grand Exchange', x: 3165, y: 3487 },
            { name: 'Varrock East Bank', x: 3253, y: 3420 },
            { name: 'Falador East Bank', x: 3013, y: 3355 },
            { name: 'Edgeville Bank', x: 3094, y: 3492 }
        ];
        
        this.init();
    }

    init() {
        this.connectWebSocket();
        this.setupEventListeners();
        this.initMap();
        this.render();
    }

    connectWebSocket() {
        this.ws = new WebSocket('ws://127.0.0.1:8777');
        
        this.ws.onopen = () => {
            this.updateState({ connectionStatus: 'connected' });
            this.elements.status.textContent = 'Connected to Microbot CommandBridge!';
        };

        this.ws.onclose = () => {
            this.updateState({ connectionStatus: 'disconnected' });
            this.elements.status.textContent = 'WebSocket connection closed. Reconnecting...';
            setTimeout(() => this.connectWebSocket(), 3000);
        };

        this.ws.onerror = () => {
            this.updateState({ connectionStatus: 'error' });
            this.elements.status.textContent = 'WebSocket error occurred';
        };

        this.ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                this.handleMessage(data);
            } catch (err) {
                console.error('Error parsing message:', err);
            }
        };
    }

    handleMessage(data) {
        if (data.command === 'BankItems' && Array.isArray(data.items)) {
            this.updateState({ bankItems: data.items });
        } else if (data.username) {
            this.updateState({ playerData: data });
        } else if (data.walk_status) {
            this.updateState({ 
                walkStatus: {
                    status: data.walk_status,
                    steps: data.steps_taken || 0,
                    total: data.total_steps || 0
                }
            });
        }
        this.render();
    }

    updateState(newState) {
        this.state = Object.assign({}, this.state, newState);
    }

    sendCommand(command, data = {}) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(Object.assign({}, { command }, data)));
        }
    }

    setupEventListeners() {
        // Bank controls
        document.getElementById('sync-bank-btn').addEventListener('click', () => {
            this.sendCommand('syncBank');
        });

        document.getElementById('withdraw-bank-btn').addEventListener('click', () => {
            const selected = this.elements.bankItemSelect.value;
            if (selected) {
                this.sendCommand('withdrawItem', { itemId: selected });
            }
        });

        // Bank actions
        document.getElementById('open-bank-btn').addEventListener('click', () => {
            this.sendCommand('openBank');
        });

        document.getElementById('close-bank-btn').addEventListener('click', () => {
            this.sendCommand('closeBank');
        });

        document.getElementById('get-bank-items-btn').addEventListener('click', () => {
            this.sendCommand('getBankItems');
        });

        // Map actions
        document.getElementById('pick-destination-btn').addEventListener('click', () => {
            this.sendCommand('pickDestination');
        });

        document.getElementById('go-bank-btn').addEventListener('click', () => {
            this.sendCommand('goToBank');
        });

        // Preset locations
        document.getElementById('go-preset-btn').addEventListener('click', () => {
            const select = document.getElementById('preset-location-select');
            const selected = select.value;
            if (selected) {
                const location = this.presetLocations.find(l => 
                    l.name.toLowerCase().replace(/\s+/g, '_') === selected);
                if (location) {
                    this.sendCommand('walkTo', { x: location.x, y: location.y });
                }
            }
        });

        // Walk coordinates
        document.getElementById('walk-btn').addEventListener('click', () => {
            const x = parseInt(document.getElementById('x-coord').value);
            const y = parseInt(document.getElementById('y-coord').value);
            if (!isNaN(x) && !isNaN(y)) {
                this.sendCommand('walkTo', { x: x, y: y });
            }
        });

        document.getElementById('stop-btn').addEventListener('click', () => {
            this.sendCommand('stopWalk');
        });
    }

    initMap() {
        const map = L.map('map').setView([3234, 3200], 5);
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        let marker = null;
        
        map.on('click', (e) => {
            const lat = e.latlng.lat;
            const lng = e.latlng.lng;
            if (marker) {
                marker.remove();
            }
            marker = L.marker([lat, lng]).addTo(map);
            this.sendCommand('walkTo', { x: Math.round(lat), y: Math.round(lng) });
        });

        this.elements.map = map;
    }

    render() {
        this.renderPlayerInfo();
        this.renderBankItems();
        this.renderWalkStatus();
    }

    renderPlayerInfo() {
        const playerData = this.state.playerData;
        const container = this.elements.playerInfo;
        
        if (!playerData) {
            container.innerHTML = '<div class="loading">No player data available</div>';
            return;
        }

        container.innerHTML = `
            <div><strong>Username:</strong> ${playerData.username || 'Unknown'}</div>
            <div><strong>Location:</strong> ${playerData.location || 'Unknown'}</div>
            <div><strong>Health:</strong> ${playerData.health || 'Unknown'}</div>
            <div><strong>Inventory:</strong> ${playerData.inventory || 'Empty'}</div>
        `;
    }

    renderBankItems() {
        const bankItems = this.state.bankItems;
        const select = this.elements.bankItemSelect;
        
        if (!bankItems || bankItems.length === 0) {
            select.innerHTML = '<option value="">No items - Sync needed</option>';
            return;
        }

        select.innerHTML = bankItems.map(item => 
            '<option value="' + item.id + '">' + item.name + ' (x' + item.quantity + ')</option>'
        ).join('');
    }

    renderWalkStatus() {
        const walkStatus = this.state.walkStatus;
        const container = this.elements.walkStatus;
        
        container.innerHTML = `
            <div><strong>Status:</strong> ${walkStatus.status}</div>
            <div><strong>Steps:</strong> ${walkStatus.steps} / ${walkStatus.total}</div>
        `;
    }
}

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    new MicrobotUI();
});
class MicrobotUI {
    constructor() {
        this.ws = null;
        this.state = {
            playerData: null,
            bankItems: [],
            walkStatus: { status: 'idle', steps: 0, total: 0 },
            connectionStatus: 'disconnected'
        };
        
        this.elements = {
            status: document.getElementById('status'),
            playerInfo: document.getElementById('player-info'),
            bankItemSelect: document.getElementById('bank-item-select'),
            walkStatus: document.getElementById('walk-status'),
            map: null
        };
        
        this.presetLocations = [
            { name: 'Grand Exchange', x: 3165, y: 3487 },
            { name: 'Varrock East Bank', x: 3253, y: 3420 },
            { name: 'Falador East Bank', x: 3013, y: 3355 },
            { name: 'Edgeville Bank', x: 3094, y: 3492 }
        ];
        
        this.init();
    }

    init() {
        this.connectWebSocket();
        this.setupEventListeners();
        this.initMap();
        this.render();
    }

    connectWebSocket() {
        this.ws = new WebSocket('ws://127.0.0.1:8777');
        
        this.ws.onopen = () => {
            this.updateState({ connectionStatus: 'connected' });
            this.elements.status.textContent = 'Connected to Microbot CommandBridge!';
        };

        this.ws.onclose = () => {
            this.updateState({ connectionStatus: 'disconnected' });
            this.elements.status.textContent = 'WebSocket connection closed. Reconnecting...';
            setTimeout(() => this.connectWebSocket(), 3000);
        };

        this.ws.onerror = () => {
            this.updateState({ connectionStatus: 'error' });
            this.elements.status.textContent = 'WebSocket error occurred';
        };

        this.ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                this.handleMessage(data);
            } catch (err) {
                console.error('Error parsing message:', err);
            }
        };
    }

    handleMessage(data) {
        if (data.command === 'BankItems' && Array.isArray(data.items)) {
            this.updateState({ bankItems: data.items });
        } else if (data.username) {
            this.updateState({ playerData: data });
        } else if (data.walk_status) {
            this.updateState({ 
                walkStatus: {
                    status: data.walk_status,
                    steps: data.steps_taken || 0,
                    total: data.total_steps || 0
                }
            });
        }
        this.render();
    }

    updateState(newState) {
        this.state = Object.assign({}, this.state, newState);
    }

    sendCommand(command, data = {}) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(Object.assign({}, { command }, data)));
        }
    }

    setupEventListeners() {
        // Bank controls
        document.getElementById('sync-bank-btn').addEventListener('click', () => {
            this.sendCommand('syncBank');
        });

        document.getElementById('withdraw-bank-btn').addEventListener('click', () => {
            const selected = this.elements.bankItemSelect.value;
            if (selected) {
                this.sendCommand('withdrawItem', { itemId: selected });
            }
        });

        // Bank actions
        document.getElementById('open-bank-btn').addEventListener('click', () => {
            this.sendCommand('openBank');
        });

        document.getElementById('close-bank-btn').addEventListener('click', () => {
            this.sendCommand('closeBank');
        });

        document.getElementById('get-bank-items-btn').addEventListener('click', () => {
            this.sendCommand('getBankItems');
        });

        // Map actions
        document.getElementById('pick-destination-btn').addEventListener('click', () => {
            this.sendCommand('pickDestination');
        });

        document.getElementById('go-bank-btn').addEventListener('click', () => {
            this.sendCommand('goToBank');
        });

        // Preset locations
        document.getElementById('go-preset-btn').addEventListener('click', () => {
            const select = document.getElementById('preset-location-select');
            const selected = select.value;
            if (selected) {
                const location = this.presetLocations.find(l => 
                    l.name.toLowerCase().replace(/\s+/g, '_') === selected);
                if (location) {
                    this.sendCommand('walkTo', { x: location.x, y: location.y });
                }
            }
        });

        // Walk coordinates
        document.getElementById('walk-btn').addEventListener('click', () => {
            const x = parseInt(document.getElementById('x-coord').value);
            const y = parseInt(document.getElementById('y-coord').value);
            if (!isNaN(x) && !isNaN(y)) {
                this.sendCommand('walkTo', { x: x, y: y });
            }
        });

        document.getElementById('stop-btn').addEventListener('click', () => {
            this.sendCommand('stopWalk');
        });
    }

    initMap() {
        const map = L.map('map').setView([3234, 3200], 5);
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        let marker = null;
        
        map.on('click', (e) => {
            const lat = e.latlng.lat;
            const lng = e.latlng.lng;
            if (marker) {
                marker.remove();
            }
            marker = L.marker([lat, lng]).addTo(map);
            this.sendCommand('walkTo', { x: Math.round(lat), y: Math.round(lng) });
        });

        this.elements.map = map;
    }

    render() {
        this.renderPlayerInfo();
        this.renderBankItems();
        this.renderWalkStatus();
    }

    renderPlayerInfo() {
        const playerData = this.state.playerData;
        const container = this.elements.playerInfo;
        
        if (!playerData) {
            container.innerHTML = '<div class="loading">No player data available</div>';
            return;
        }

        container.innerHTML = `
            <div><strong>Username:</strong> ${playerData.username || 'Unknown'}</div>
            <div><strong>Location:</strong> ${playerData.location || 'Unknown'}</div>
            <div><strong>Health:</strong> ${playerData.health || 'Unknown'}</div>
            <div><strong>Inventory:</strong> ${playerData.inventory || 'Empty'}</div>
        `;
    }

    renderBankItems() {
        const bankItems = this.state.bankItems;
        const select = this.elements.bankItemSelect;
        
        if (!bankItems || bankItems.length === 0) {
            select.innerHTML = '<option value="">No items - Sync needed</option>';
            return;
        }

        select.innerHTML = bankItems.map(item => 
            '<option value="' + item.id + '">' + item.name + ' (x' + item.quantity + ')</option>'
        ).join('');
    }

    renderWalkStatus() {
        const walkStatus = this.state.walkStatus;
        const container = this.elements.walkStatus;
        
        container.innerHTML = `
            <div><strong>Status:</strong> ${walkStatus.status}</div>
            <div><strong>Steps:</strong> ${walkStatus.steps} / ${walkStatus.total}</div>
        `;
    }
}

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    new MicrobotUI();
});
class MicrobotUI {
    constructor() {
        this.ws = null;
        this.state = {
            playerData: null,
            bankItems: [],
            walkStatus: { status: 'idle', steps: 0, total: 0 },
            connectionStatus: 'disconnected'
        };
        
        this.elements = {
            status: document.getElementById('status'),
            playerInfo: document.getElementById('player-info'),
            bankItemSelect: document.getElementById('bank-item-select'),
            walkStatus: document.getElementById('walk-status'),
            map: null
        };
        
        this.presetLocations = [
            { name: 'Grand Exchange', x: 3165, y: 3487 },
            { name: 'Varrock East Bank', x: 3253, y: 3420 },
            { name: 'Falador East Bank', x: 3013, y: 3355 },
            { name: 'Edgeville Bank', x: 3094, y: 3492 }
        ];
        
        this.init();
    }

    init() {
        this.connectWebSocket();
        this.setupEventListeners();
        this.initMap();
        this.render();
    }

    connectWebSocket() {
        this.ws = new WebSocket('ws://127.0.0.1:8777');
        
        this.ws.onopen = () => {
            this.updateState({ connectionStatus: 'connected' });
            this.elements.status.textContent = 'Connected to Microbot CommandBridge!';
        };

        this.ws.onclose = () => {
            this.updateState({ connectionStatus: 'disconnected' });
            this.elements.status.textContent = 'WebSocket connection closed. Reconnecting...';
            setTimeout(() => this.connectWebSocket(), 3000);
        };

        this.ws.onerror = () => {
            this.updateState({ connectionStatus: 'error' });
            this.elements.status.textContent = 'WebSocket error occurred';
        };

        this.ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                this.handleMessage(data);
            } catch (err) {
                console.error('Error parsing message:', err);
            }
        };
    }

    handleMessage(data) {
        if (data.command === 'BankItems' && Array.isArray(data.items)) {
            this.updateState({ bankItems: data.items });
        } else if (data.username) {
            this.updateState({ playerData: data });
        } else if (data.walk_status) {
            this.updateState({ 
                walkStatus: {
                    status: data.walk_status,
                    steps: data.steps_taken || 0,
                    total: data.total_steps || 0
                }
            });
        }
        this.render();
    }

    updateState(newState) {
        this.state = Object.assign({}, this.state, newState);
    }

    sendCommand(command, data = {}) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(Object.assign({}, { command }, data)));
        }
    }

    setupEventListeners() {
        // Bank controls
        document.getElementById('sync-bank-btn').addEventListener('click', () => {
            this.sendCommand('syncBank');
        });

        document.getElementById('withdraw-bank-btn').addEventListener('click', () => {
            const selected = this.elements.bankItemSelect.value;
            if (selected) {
                this.sendCommand('withdrawItem', { itemId: selected });
            }
        });

        // Bank actions
        document.getElementById('open-bank-btn').addEventListener('click', () => {
            this.sendCommand('openBank');
        });

        document.getElementById('close-bank-btn').addEventListener('click', () => {
            this.sendCommand('closeBank');
        });

        document.getElementById('get-bank-items-btn').addEventListener('click', () => {
            this.sendCommand('getBankItems');
        });

        // Map actions
        document.getElementById('pick-destination-btn').addEventListener('click', () => {
            this.sendCommand('pickDestination');
        });

        document.getElementById('go-bank-btn').addEventListener('click', () => {
            this.sendCommand('goToBank');
        });

        // Preset locations
        document.getElementById('go-preset-btn').addEventListener('click', () => {
            const select = document.getElementById('preset-location-select');
            const selected = select.value;
            if (selected) {
                const location = this.presetLocations.find(l => 
                    l.name.toLowerCase().replace(/\s+/g, '_') === selected);
                if (location) {
                    this.sendCommand('walkTo', { x: location.x, y: location.y });
                }
            }
        });

        // Walk coordinates
        document.getElementById('walk-btn').addEventListener('click', () => {
            const x = parseInt(document.getElementById('x-coord').value);
            const y = parseInt(document.getElementById('y-coord').value);
            if (!isNaN(x) && !isNaN(y)) {
                this.sendCommand('walkTo', { x: x, y: y });
            }
        });

        document.getElementById('stop-btn').addEventListener('click', () => {
            this.sendCommand('stopWalk');
        });
    }

    initMap() {
        const map = L.map('map').setView([3234, 3200], 5);
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        let marker = null;
        
        map.on('click', (e) => {
            const lat = e.latlng.lat;
            const lng = e.latlng.lng;
            if (marker) {
                marker.remove();
            }
            marker = L.marker([lat, lng]).addTo(map);
            this.sendCommand('walkTo', { x: Math.round(lat), y: Math.round(lng) });
        });

        this.elements.map = map;
    }

    render() {
        this.renderPlayerInfo();
        this.renderBankItems();
        this.renderWalkStatus();
    }

    renderPlayerInfo() {
        const playerData = this.state.playerData;
        const container = this.elements.playerInfo;
        
        if (!playerData) {
            container.innerHTML = '<div class="loading">No player data available</div>';
            return;
        }

        container.innerHTML = `
            <div><strong>Username:</strong> ${playerData.username || 'Unknown'}</div>
            <div><strong>Location:</strong> ${playerData.location || 'Unknown'}</div>
            <div><strong>Health:</strong> ${playerData.health || 'Unknown'}</div>
            <div><strong>Inventory:</strong> ${playerData.inventory || 'Empty'}</div>
        `;
    }

    renderBankItems() {
        const bankItems = this.state.bankItems;
        const select = this.elements.bankItemSelect;
        
        if (!bankItems || bankItems.length === 0) {
            select.innerHTML = '<option value="">No items - Sync needed</option>';
            return;
        }

        select.innerHTML = bankItems.map(item => 
            '<option value="' + item.id + '">' + item.name + ' (x' + item.quantity + ')</option>'
        ).join('');
    }

    renderWalkStatus() {
        const walkStatus = this.state.walkStatus;
        const container = this.elements.walkStatus;
        
        container.innerHTML = `
            <div><strong>Status:</strong> ${walkStatus.status}</div>
            <div><strong>Steps:</strong> ${walkStatus.steps} / ${walkStatus.total}</div>
        `;
    }
}

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    new MicrobotUI();
});
class MicrobotUI {
    constructor() {
        this.ws = null;
        this.state = {
            playerData: null,
            bankItems: [],
            walkStatus: { status: 'idle', steps: 0, total: 0 },
            connectionStatus: 'disconnected'
        };
        
        this.elements = {
            status: document.getElementById('status'),
            playerInfo: document.getElementById('player-info'),
            bankItemSelect: document.getElementById('bank-item-select'),
            walkStatus: document.getElementById('walk-status'),
            map: null
        };
        
        this.presetLocations = [
            { name: 'Grand Exchange', x: 3165, y: 3487 },
            { name: 'Varrock East Bank', x: 3253, y: 3420 },
            { name: 'Falador East Bank', x: 3013, y: 3355 },
            { name: 'Edgeville Bank', x: 3094, y: 3492 }
        ];
        
        this.init();
    }

    init() {
        this.connectWebSocket();
        this.setupEventListeners();
        this.initMap();
        this.render();
    }

    connectWebSocket() {
        this.ws = new WebSocket('ws://127.0.0.1:8777');
        
        this.ws.onopen = () => {
            this.updateState({ connectionStatus: 'connected' });
            this.elements.status.textContent = 'Connected to Microbot CommandBridge!';
        };

        this.ws.onclose = () => {
            this.updateState({ connectionStatus: 'disconnected' });
            this.elements.status.textContent = 'WebSocket connection closed. Reconnecting...';
            setTimeout(() => this.connectWebSocket(), 3000);
        };

        this.ws.onerror = () => {
            this.updateState({ connectionStatus: 'error' });
            this.elements.status.textContent = 'WebSocket error occurred';
        };

        this.ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                this.handleMessage(data);
            } catch (err) {
                console.error('Error parsing message:', err);
            }
        };
    }

    handleMessage(data) {
        if (data.command === 'BankItems' && Array.isArray(data.items)) {
            this.updateState({ bankItems: data.items });
        } else if (data.username) {
            this.updateState({ playerData: data });
        } else if (data.walk_status) {
            this.updateState({ 
                walkStatus: {
                    status: data.walk_status,
                    steps: data.steps_taken || 0,
                    total: data.total_steps || 0
                }
            });
        }
        this.render();
    }

    updateState(newState) {
        this.state = Object.assign({}, this.state, newState);
    }

    sendCommand(command, data = {}) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(Object.assign({}, { command }, data)));
        }
    }

    setupEventListeners() {
        // Bank controls
        document.getElementById('sync-bank-btn').addEventListener('click', () => {
            this.sendCommand('syncBank');
        });

        document.getElementById('withdraw-bank-btn').addEventListener('click', () => {
            const selected = this.elements.bankItemSelect.value;
            if (selected) {
                this.sendCommand('withdrawItem', { itemId: selected });
            }
        });

        // Bank actions
        document.getElementById('open-bank-btn').addEventListener('click', () => {
            this.sendCommand('openBank');
        });

        document.getElementById('close-bank-btn').addEventListener('click', () => {
            this.sendCommand('closeBank');
        });

        document.getElementById('get-bank-items-btn').addEventListener('click', () => {
            this.sendCommand('getBankItems');
        });

        // Map actions
        document.getElementById('pick-destination-btn').addEventListener('click', () => {
            this.sendCommand('pickDestination');
        });

        document.getElementById('go-bank-btn').addEventListener('click', () => {
            this.sendCommand('goToBank');
        });

        // Preset locations
        document.getElementById('go-preset-btn').addEventListener('click', () => {
            const select = document.getElementById('preset-location-select');
            const selected = select.value;
            if (selected) {
                const location = this.presetLocations.find(l => 
                    l.name.toLowerCase().replace(/\s+/g, '_') === selected);
                if (location) {
                    this.sendCommand('walkTo', { x: location.x, y: location.y });
                }
            }
        });

        // Walk coordinates
        document.getElementById('walk-btn').addEventListener('click', () => {
            const x = parseInt(document.getElementById('x-coord').value);
            const y = parseInt(document.getElementById('y-coord').value);
            if (!isNaN(x) && !isNaN(y)) {
                this.sendCommand('walkTo', { x: x, y: y });
            }
        });

        document.getElementById('stop-btn').addEventListener('click', () => {
            this.sendCommand('stopWalk');
        });
    }

    initMap() {
        const map = L.map('map').setView([3234, 3200], 5);
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        let marker = null;
        
        map.on('click', (e) => {
            const lat = e.latlng.lat;
            const lng = e.latlng.lng;
            if (marker) {
                marker.remove();
            }
            marker = L.marker([lat, lng]).addTo(map);
            this.sendCommand('walkTo', { x: Math.round(lat), y: Math.round(lng) });
        });

        this.elements.map = map;
    }

    render() {
        this.renderPlayerInfo();
        this.renderBankItems();
        this.renderWalkStatus();
    }

    renderPlayerInfo() {
        const playerData = this.state.playerData;
        const container = this.elements.playerInfo;
        
        if (!playerData) {
            container.innerHTML = '<div class="loading">No player data available</div>';
            return;
        }

        container.innerHTML = `
            <div><strong>Username:</strong> ${playerData.username || 'Unknown'}</div>
            <div><strong>Location:</strong> ${playerData.location || 'Unknown'}</div>
            <div><strong>Health:</strong> ${playerData.health || 'Unknown'}</div>
            <div><strong>Inventory:</strong> ${playerData.inventory || 'Empty'}</div>
        `;
    }

    renderBankItems() {
        const bankItems = this.state.bankItems;
        const select = this.elements.bankItemSelect;
        
        if (!bankItems || bankItems.length === 0) {
            select.innerHTML = '<option value="">No items - Sync needed</option>';
            return;
        }

        select.innerHTML = bankItems.map(item => 
            '<option value="' + item.id + '">' + item.name + ' (x' + item.quantity + ')</option>'
        ).join('');
    }

    renderWalkStatus() {
        const walkStatus = this.state.walkStatus;
        const container = this.elements.walkStatus;
        
        container.innerHTML = `
            <div><strong>Status:</strong> ${walkStatus.status}</div>
            <div><strong>Steps:</strong> ${walkStatus.steps} / ${walkStatus.total}</div>
        `;
    }
}

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    new MicrobotUI();
});

class MicrobotUI {
  constructor() {
    this.ws = null;
    this.state = {
      playerData: null,
      bankItems: [],
      walkStatus: { status: 'idle', steps: 0, total: 0 },
      connectionStatus: 'disconnected'

  playersDiv.innerHTML = html;
  attachBankDropdownHandlers();

  // Attach handlers for preset location buttons
  document.querySelectorAll('.teleport-btn').forEach(btn => {
    btn.onclick = () => {
      if(window._ws && window._ws.readyState === 1) {
        window._ws.send(JSON.stringify({ command: "WebWalk", location: btn.dataset.location }));
      }
    };
  });

  // Handler for dropdown Go button
  const goBtn = document.getElementById('go-location-btn');
  const select = document.getElementById('location-select');
  if (goBtn && select) {
    goBtn.onclick = () => {
      const loc = select.value;
      if (loc && window._ws && window._ws.readyState === 1) {
        window._ws.send(JSON.stringify({ command: "WebWalk", location: loc }));
      }
    };
  }

  // Handler for map picker button
  const mapBtn = document.getElementById('map-picker-btn');
  if (mapBtn) {
    mapBtn.onclick = () => {
      showMapOverlay();
    };
  }

  // Handler for Go to Closest Bank and Open button
  const goBankBtn = document.getElementById('go-bank-btn');
  if (goBankBtn) {
    goBankBtn.onclick = () => {
      if (window._ws && window._ws.readyState === 1) {
        window._ws.send(JSON.stringify({ command: "GoToBankAndGetItems" }));
      }
    };
  }

  // Handler for walk-to-coords (unchanged)
  const walkBtn = document.getElementById('walk-coords-btn');
  if (walkBtn) {
    walkBtn.onclick = () => {
      const x = parseInt(document.getElementById('walk-x').value);
      const y = parseInt(document.getElementById('walk-y').value);
      const plane = parseInt(document.getElementById('walk-plane').value) || 0;
      if (!isNaN(x) && !isNaN(y) && window._ws && window._ws.readyState === 1) {
        window._ws.send(JSON.stringify({ command: "WebWalk", x, y, plane }));
      }
    };
  }
}

function connect() {
  let ws;
  try {
    ws = new WebSocket('ws://127.0.0.1:8777');
  } catch (e) {
    statusDiv.textContent = 'WebSocket connection failed.';
    return;
  }
  window._ws = ws;
  ws.onopen = () => {
    statusDiv.textContent = 'Connected to Microbot CommandBridge!';
  };

  ws.onclose = () => {
    statusDiv.textContent = 'WebSocket connection closed.';
  };

  ws.onerror = (e) => {
    statusDiv.textContent = 'WebSocket error.';
  };

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      if (data.command === "BankItems" && Array.isArray(data.items)) {
        latestBankItems = data.items;
        renderPlayers();
        attachBankDropdownHandlers();
      }
      if (data.username) {
        latestPlayerData = data;
        updatePlayerInfo();
      } else if (data.walk_status) {
        walkStatus = data.walk_status;
        walkStepsTaken = typeof data.steps_taken === 'number' ? data.steps_taken : 0;
        walkTotalSteps = typeof data.total_steps === 'number' ? data.total_steps : 0;
        updateWalkStatus();
      }
    } catch (err) {
      // Ignore non-JSON messages
    }
  };
}

// No-op function - legacy UI removed
function renderStaticUI() {
  // Intentionally empty - new UI is handled by renderPlayers()
}

// Clean startup
window.addEventListener('DOMContentLoaded', () => {
  renderStaticUI();
  connect();
  renderPlayers();
  updatePlayerInfo();
  updateWalkStatus();
});

// --- LEAFLET MAP INTEGRATION ---
let map, mapMarker;

function initMap() {
  // Approximate bounds for RuneScape world map
  const mapBounds = [[0, 0], [10000, 10000]];
  map = L.map('map', {
    crs: L.CRS.Simple,
    minZoom: -2,
    maxZoom: 4,
    zoomSnap: 0.25,
    attributionControl: false
  });
  const imgUrl = 'https://oldschool.runescape.wiki/images/7/77/World_map_full.png';
  const imgWidth = 9472, imgHeight = 6144;
  const imageBounds = [[0, 0], [imgHeight, imgWidth]];
  L.imageOverlay(imgUrl, imageBounds).addTo(map);
  map.fitBounds(imageBounds);

  map.on('click', function(e) {
    const y = Math.round(e.latlng.lat);
    const x = Math.round(e.latlng.lng);
    const plane = 0;
    if (mapMarker) map.removeLayer(mapMarker);
    mapMarker = L.marker([y, x]).addTo(map);
    if (window._ws && window._ws.readyState === 1) {
      window._ws.send(JSON.stringify({ command: "WebWalk", x, y, plane }));
      statusDiv.textContent = `Walking to (${x}, ${y}, plane ${plane})...`;
    }
  });
}
