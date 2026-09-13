/**
 * SECUREBANK ADMIN MANAGEMENT SUITE CONTROLLER (admin.js)
 * 
 * Manages:
 * 1. Bank aggregate metrics & vault calculations (with demo fallbacks).
 * 2. Multi-tab navigation (Dashboard, Customers, Accounts, Transactions, Audit Logs, Reports).
 * 3. Master account moderation (Freeze/Block vs Unblock).
 * 4. Verified customer directory and search.
 * 5. Global transaction audit stream.
 */

let currentAdmin = null;
let allAccounts = [];
let allCustomers = [];
let allTransactions = [];

// Demo Fallback Data for College Simulation Presentation
const DEMO_CUSTOMERS = [
  { customerId: 1, fullName: 'Alice Johnson', email: 'alice@securebank.com', phone: '+91 98765 01010', address: '742 Evergreen Terrace, Springfield', createdAt: '2026-08-01T10:00:00Z', accountNumber: 'SB-1001-SAV', status: 'ACTIVE' },
  { customerId: 2, fullName: 'Bob Smith', email: 'bob@securebank.com', phone: '+91 98765 02020', address: '123 Fake Street, Metropolis', createdAt: '2026-08-05T12:30:00Z', accountNumber: 'SB-2001-CUR', status: 'ACTIVE' },
  { customerId: 3, fullName: 'Charlie Brown', email: 'charlie@securebank.com', phone: '+91 98765 03030', address: '456 Elm Street, Gotham', createdAt: '2026-08-10T14:15:00Z', accountNumber: 'SB-3001-SAV', status: 'ACTIVE' },
  { customerId: 4, fullName: 'Akash Sharma', email: 'akash@example.com', phone: '+91 98765 43210', address: 'B-12 Preet Vihar, New Delhi', createdAt: '2026-08-18T10:45:00Z', accountNumber: 'SB-4819-2041', status: 'ACTIVE' }
];

const DEMO_ACCOUNTS = [
  { accountNumber: 'SB-1001-SAV', customerName: 'Alice Johnson', accountType: 'SAVINGS', balance: 45000.00, totalAvailableFunds: 45000.00, status: 'ACTIVE' },
  { accountNumber: 'SB-2001-CUR', customerName: 'Bob Smith', accountType: 'CURRENT', balance: 12000.00, totalAvailableFunds: 22000.00, status: 'ACTIVE' },
  { accountNumber: 'SB-3001-SAV', customerName: 'Charlie Brown', accountType: 'SAVINGS', balance: 89000.00, totalAvailableFunds: 89000.00, status: 'ACTIVE' },
  { accountNumber: 'SB-4819-2041', customerName: 'Akash Sharma', accountType: 'SAVINGS', balance: 50000.00, totalAvailableFunds: 50000.00, status: 'ACTIVE' }
];

const DEMO_TRANSACTIONS = [
  { referenceNumber: 'TXN-902148', timestamp: '2026-08-18T10:45:00Z', sourceAccountNumber: 'External', destinationAccountNumber: 'SB-4819-2041', transactionType: 'DEPOSIT', amount: 50000.00, status: 'COMPLETED', description: 'Opening Account Deposit' },
  { referenceNumber: 'TXN-881204', timestamp: '2026-08-18T09:30:00Z', sourceAccountNumber: 'SB-1001-SAV', destinationAccountNumber: 'SB-2001-CUR', transactionType: 'TRANSFER', amount: 2500.00, status: 'COMPLETED', description: 'P2P Transfer Payment' },
  { referenceNumber: 'TXN-773192', timestamp: '2026-08-17T16:20:00Z', sourceAccountNumber: 'SB-2001-CUR', destinationAccountNumber: 'ATM-04', transactionType: 'WITHDRAWAL', amount: 1000.00, status: 'COMPLETED', description: 'ATM Cash Withdrawal' },
  { referenceNumber: 'TXN-664019', timestamp: '2026-08-17T11:10:00Z', sourceAccountNumber: 'External', destinationAccountNumber: 'SB-3001-SAV', transactionType: 'DEPOSIT', amount: 15000.00, status: 'COMPLETED', description: 'Wire Transfer Credit' }
];

document.addEventListener('DOMContentLoaded', async () => {
  currentAdmin = requireAuth('ROLE_ADMIN');
  if (!currentAdmin) return;

  const adminName = currentAdmin.fullName || currentAdmin.email.split('@')[0] || 'Administrator';
  document.getElementById('adminUsername').innerText = adminName;
  const titleElem = document.getElementById('adminWelcomeTitle');
  if (titleElem) titleElem.innerText = adminName;

  await loadAdminMetrics();
  await loadAccountsTable();
  await loadCustomersTable();
  await loadGlobalTransactions();
});

/**
 * Loads System Metrics
 */
async function loadAdminMetrics() {
  try {
    const res = await API.admin.getStatistics();
    const stats = res.data;

    document.getElementById('statVaultReserves').innerText = `₹${(stats.totalVaultBalance || 84520000).toLocaleString('en-IN')}`;
    document.getElementById('statCustomers').innerText = stats.totalCustomers || '1,248';
    document.getElementById('statAccounts').innerText = stats.totalAccounts || '1,196';
    document.getElementById('statTxnCount').innerText = stats.totalTransactionsCount || '327';
  } catch (error) {
    // Default demo values
    document.getElementById('statVaultReserves').innerText = '₹8,45,20,000';
    document.getElementById('statCustomers').innerText = '1,248';
    document.getElementById('statAccounts').innerText = '1,196';
    document.getElementById('statTxnCount').innerText = '327';
  }
}

/**
 * Loads Master Accounts Table
 */
async function loadAccountsTable() {
  const tbody = document.getElementById('adminAccountsTableBody');
  try {
    const res = await API.admin.getAllAccounts();
    allAccounts = (res.data && res.data.length > 0) ? res.data : DEMO_ACCOUNTS;
    renderAccountsTable(allAccounts);
  } catch (error) {
    allAccounts = DEMO_ACCOUNTS;
    renderAccountsTable(allAccounts);
  }
}

function renderAccountsTable(accounts) {
  const tbody = document.getElementById('adminAccountsTableBody');
  tbody.innerHTML = '';

  if (!accounts || accounts.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 20px;">No accounts found.</td></tr>';
    return;
  }

  accounts.forEach(acc => {
    const isBlocked = acc.status === 'BLOCKED';
    const row = document.createElement('tr');
    row.innerHTML = `
      <td><strong>${acc.accountNumber}</strong></td>
      <td>${acc.customerName}</td>
      <td><span class="badge ${acc.accountType === 'SAVINGS' ? 'badge-success' : 'badge-warning'}">${acc.accountType}</span></td>
      <td style="font-weight: 700;">₹${acc.balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
      <td style="color: var(--accent-600);">₹${(acc.totalAvailableFunds || acc.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
      <td><span class="badge ${isBlocked ? 'badge-danger' : 'badge-success'}">${acc.status}</span></td>
      <td>
        <button onclick="handleToggleBlock('${acc.accountNumber}', '${acc.status}')" 
                class="btn ${isBlocked ? 'btn-primary' : 'btn-danger'}" 
                style="padding: 4px 10px; font-size: 0.75rem;">
          ${isBlocked ? '🟢 Unfreeze' : '🚫 Freeze / Block'}
        </button>
      </td>
    `;
    tbody.appendChild(row);
  });
}

/**
 * Toggles Account Block/Unblock Status
 */
async function handleToggleBlock(accountNumber, currentStatus) {
  const action = currentStatus === 'BLOCKED' ? 'unblock' : 'block';
  if (!confirm(`Are you sure you want to ${action.toUpperCase()} account ${accountNumber}?`)) return;

  try {
    if (currentStatus === 'BLOCKED') {
      await API.admin.unblockAccount(accountNumber);
      showToast(`Account ${accountNumber} has been UNBLOCKED.`, 'success');
    } else {
      await API.admin.blockAccount(accountNumber);
      showToast(`Account ${accountNumber} has been BLOCKED.`, 'success');
    }
  } catch (error) {
    // Local demo update
    const target = allAccounts.find(a => a.accountNumber === accountNumber);
    if (target) {
      target.status = (currentStatus === 'BLOCKED') ? 'ACTIVE' : 'BLOCKED';
      showToast(`Account ${accountNumber} status changed to ${target.status} (Demo simulation)!`, 'success');
    }
  }
  await loadAccountsTable();
}

/**
 * Loads Customer Directory
 */
async function loadCustomersTable() {
  const tbody = document.getElementById('adminCustomersTableBody');
  try {
    const res = await API.admin.getAllCustomers();
    allCustomers = (res.data && res.data.length > 0) ? res.data : DEMO_CUSTOMERS;
    renderCustomersTable(allCustomers);
  } catch (error) {
    allCustomers = DEMO_CUSTOMERS;
    renderCustomersTable(allCustomers);
  }
}

function renderCustomersTable(customers) {
  const tbody = document.getElementById('adminCustomersTableBody');
  tbody.innerHTML = '';

  if (!customers || customers.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 20px;">No registered customers.</td></tr>';
    return;
  }

  customers.forEach(c => {
    const row = document.createElement('tr');
    row.innerHTML = `
      <td><code>#${c.customerId}</code></td>
      <td><strong>${c.fullName}</strong></td>
      <td>${c.email}</td>
      <td><code style="background: #f1f5f9; padding: 2px 6px; border-radius: 4px;">${c.accountNumber || 'SB-1001-SAV'}</code></td>
      <td><span class="badge badge-success">${c.status || 'ACTIVE'}</span></td>
      <td style="font-size: 0.85rem; color: var(--text-muted);">${new Date(c.createdAt || Date.now()).toLocaleDateString()}</td>
    `;
    tbody.appendChild(row);
  });
}

/**
 * Loads Global Transaction Audit Feed
 */
async function loadGlobalTransactions() {
  const tbody = document.getElementById('adminTxnTableBody');
  try {
    const res = await API.admin.getAllTransactions();
    allTransactions = (res.data && res.data.length > 0) ? res.data : DEMO_TRANSACTIONS;
    renderGlobalTransactions(allTransactions);
  } catch (error) {
    allTransactions = DEMO_TRANSACTIONS;
    renderGlobalTransactions(allTransactions);
  }
}

function renderGlobalTransactions(transactions) {
  const tbody = document.getElementById('adminTxnTableBody');
  tbody.innerHTML = '';

  if (!transactions || transactions.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 20px;">No transaction records.</td></tr>';
    return;
  }

  transactions.forEach(t => {
    const parties = `${t.sourceAccountNumber || 'External'} → ${t.destinationAccountNumber || 'External'}`;
    const row = document.createElement('tr');
    row.innerHTML = `
      <td><code>${t.referenceNumber}</code></td>
      <td style="font-size: 0.85rem; color: var(--text-muted);">${new Date(t.timestamp || Date.now()).toLocaleString()}</td>
      <td><small>${parties}</small></td>
      <td><span class="badge badge-success">${t.transactionType}</span></td>
      <td style="font-weight: 700;">₹${t.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
      <td><span class="badge badge-success">${t.status || 'COMPLETED'}</span></td>
      <td>${t.description || 'General Banking Transaction'}</td>
    `;
    tbody.appendChild(row);
  });
}

/**
 * Local Filter Handlers
 */
function filterAdminAccounts() {
  const q = document.getElementById('accountSearchInput').value.toLowerCase();
  renderAccountsTable(allAccounts.filter(a => 
    a.accountNumber.toLowerCase().includes(q) || a.customerName.toLowerCase().includes(q)
  ));
}

function filterAdminCustomers() {
  const q = document.getElementById('customerSearchInput').value.toLowerCase();
  renderCustomersTable(allCustomers.filter(c => 
    c.fullName.toLowerCase().includes(q) || c.email.toLowerCase().includes(q) || (c.phone && c.phone.toLowerCase().includes(q))
  ));
}

function filterAdminTransactions() {
  const q = document.getElementById('globalTxnSearchInput').value.toLowerCase();
  renderGlobalTransactions(allTransactions.filter(t => 
    t.referenceNumber.toLowerCase().includes(q) || (t.description && t.description.toLowerCase().includes(q)) ||
    (t.sourceAccountNumber && t.sourceAccountNumber.toLowerCase().includes(q)) ||
    (t.destinationAccountNumber && t.destinationAccountNumber.toLowerCase().includes(q))
  ));
}

/**
 * Tab Switcher
 */
function switchAdminTab(tabName) {
  const tabs = ['dashboard', 'customers', 'accounts', 'transactions', 'audit', 'reports'];
  tabs.forEach(t => {
    const tabEl = document.getElementById(`tabContent-${t}`);
    if (tabEl) tabEl.style.display = (t === tabName) ? 'block' : 'none';

    const nav = document.getElementById(`nav-${t}`);
    if (nav) {
      if (t === tabName) nav.classList.add('active');
      else nav.classList.remove('active');
    }
  });
  window.scrollTo({ top: 0, behavior: 'smooth' });
}
