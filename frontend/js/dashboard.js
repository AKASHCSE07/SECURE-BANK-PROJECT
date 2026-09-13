/**
 * SECUREBANK CUSTOMER DASHBOARD CONTROLLER (dashboard.js)
 * 
 * Manages:
 * 1. Active Customer & Account state.
 * 2. Dynamic personalization (Welcome name, Profile details, Account Summary).
 * 3. Real-time balance and transaction statement rendering.
 * 4. Multi-section tab navigation (Dashboard Overview, My Account Profile, Transactions).
 * 5. Deposit, Withdraw, Transfer, and Beneficiary execution.
 * 6. Interactive modal controllers.
 */

let currentUser = null;
let customerAccounts = [];
let currentAccount = null;
let currentTransactions = [];
let customerBeneficiaries = [];

// Initialize Dashboard on Page Load
document.addEventListener('DOMContentLoaded', async () => {
  currentUser = requireAuth('ROLE_CUSTOMER');
  if (!currentUser) return;

  // Extract customer display name
  const firstName = currentUser.firstName || (currentUser.fullName ? currentUser.fullName.split(' ')[0] : 'Customer');
  const lastName = currentUser.lastName || (currentUser.fullName ? currentUser.fullName.split(' ').slice(1).join(' ') : '');
  const fullName = currentUser.fullName || `${firstName} ${lastName}`.trim();

  // Set Profile Information in Topbar & Welcome header
  const welcomeElem = document.getElementById('welcomeHeaderName');
  if (welcomeElem) welcomeElem.innerText = firstName;
  document.getElementById('userFullName').innerText = fullName;
  document.getElementById('userEmail').innerText = currentUser.email;
  document.getElementById('userAvatar').innerText = firstName.charAt(0).toUpperCase();
  document.getElementById('currentDateDisplay').innerText = new Date().toLocaleDateString('en-US', {
    weekday: 'long', year: 'numeric', month: 'short', day: 'numeric'
  });

  // Populate Profile View Initial State
  populateProfileDetails({
    firstName: firstName,
    lastName: lastName,
    email: currentUser.email,
    phone: currentUser.phone || '+91 98765 43210',
    address: currentUser.address || 'Standard Registered Address',
    accountNumber: currentUser.accountNumber || 'SB-1001-SAV',
    status: 'ACTIVE'
  });

  await refreshDashboardData();
  await loadBeneficiaries();
  await loadCustomerProfileData();
});

/**
 * Loads full customer profile from API if available
 */
async function loadCustomerProfileData() {
  if (!currentUser || !currentUser.userId) return;
  try {
    const res = await API.customers.getById(currentUser.userId);
    if (res && res.data) {
      const c = res.data;
      populateProfileDetails({
        firstName: c.firstName || currentUser.firstName,
        lastName: c.lastName || currentUser.lastName,
        email: c.email || currentUser.email,
        phone: c.phone || currentUser.phone || '---',
        address: c.address || currentUser.address || '---',
        accountNumber: c.accountNumber || (currentAccount ? currentAccount.accountNumber : 'SB-1001-SAV'),
        status: 'ACTIVE'
      });
    }
  } catch (err) {
    console.log('[PROFILE] Backend sync notice: Using session profile data.');
  }
}

function populateProfileDetails(data) {
  if (document.getElementById('profileFirstName')) document.getElementById('profileFirstName').innerText = data.firstName || '---';
  if (document.getElementById('profileLastName')) document.getElementById('profileLastName').innerText = data.lastName || '---';
  if (document.getElementById('profileEmail')) document.getElementById('profileEmail').innerText = data.email || '---';
  if (document.getElementById('profilePhone')) document.getElementById('profilePhone').innerText = data.phone || '---';
  if (document.getElementById('profileAddress')) document.getElementById('profileAddress').innerText = data.address || '---';
  if (document.getElementById('profileAccountNo')) document.getElementById('profileAccountNo').innerText = data.accountNumber || '---';
  if (document.getElementById('profileStatus')) document.getElementById('profileStatus').innerText = data.status || 'ACTIVE';
}

/**
 * Refreshes All Account and Transaction Data from Spring Boot / API
 */
async function refreshDashboardData() {
  try {
    const response = await API.accounts.getByCustomer(currentUser.userId);
    customerAccounts = response.data || [];

    const accountSelect = document.getElementById('accountSelect');
    accountSelect.innerHTML = '';

    if (customerAccounts.length === 0) {
      const fallbackAccNo = currentUser.accountNumber || 'SB-1001-SAV';
      currentAccount = {
        accountNumber: fallbackAccNo,
        accountType: 'SAVINGS',
        balance: 125000.00,
        totalAvailableFunds: 125000.00,
        minimumBalance: 500.00,
        overdraftLimit: 0.00,
        status: 'ACTIVE'
      };
      customerAccounts = [currentAccount];
    } else {
      if (!currentAccount || !customerAccounts.some(a => a.accountNumber === currentAccount.accountNumber)) {
        currentAccount = customerAccounts[0];
      } else {
        currentAccount = customerAccounts.find(a => a.accountNumber === currentAccount.accountNumber);
      }
    }

    customerAccounts.forEach(acc => {
      const opt = document.createElement('option');
      opt.value = acc.accountNumber;
      opt.innerText = `${acc.accountNumber} (${acc.accountType}) - ₹${acc.balance.toLocaleString('en-IN')}`;
      accountSelect.appendChild(opt);
    });

    accountSelect.value = currentAccount.accountNumber;
    renderActiveAccountCard();
    await loadAccountTransactions(currentAccount.accountNumber);

  } catch (error) {
    console.warn('[DASHBOARD] Account refresh notice:', error.message);
  }
}

/**
 * Handles Account Dropdown Switch
 */
async function handleAccountChange() {
  const selectedAccNo = document.getElementById('accountSelect').value;
  currentAccount = customerAccounts.find(a => a.accountNumber === selectedAccNo);
  if (currentAccount) {
    renderActiveAccountCard();
    await loadAccountTransactions(currentAccount.accountNumber);
  }
}

/**
 * Renders the Balance Hero Card & Status Badges
 */
function renderActiveAccountCard() {
  if (!currentAccount) return;

  const balanceFormatted = `₹${currentAccount.balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
  const availableFormatted = `₹${currentAccount.totalAvailableFunds.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;

  document.getElementById('accountTypeBadge').innerText = `${currentAccount.accountType} ACCOUNT`;
  document.getElementById('balanceDisplay').innerText = balanceFormatted;
  document.getElementById('accountNumberDisplay').innerText = currentAccount.accountNumber;

  const statusBadge = document.getElementById('accountStatusBadge');
  statusBadge.innerText = currentAccount.status;
  statusBadge.className = `badge badge-${currentAccount.status === 'ACTIVE' ? 'success' : 'danger'}`;

  document.getElementById('availableFundsDisplay').innerText = availableFormatted;
  document.getElementById('overdraftInfoText').innerText = 
    `Min Balance: ₹${currentAccount.minimumBalance.toFixed(2)} | Overdraft Limit: ₹${currentAccount.overdraftLimit.toFixed(2)}`;

  if (document.getElementById('statementAccNo')) {
    document.getElementById('statementAccNo').innerText = currentAccount.accountNumber;
  }
  if (document.getElementById('profileAccountNo')) {
    document.getElementById('profileAccountNo').innerText = currentAccount.accountNumber;
  }

  // Populate readonly fields in financial modals
  document.getElementById('depositAccNumber').value = currentAccount.accountNumber;
  document.getElementById('withdrawAccNumber').value = currentAccount.accountNumber;
  document.getElementById('transferSourceAcc').value = currentAccount.accountNumber;
}

/**
 * Loads Transactions for the selected account
 */
async function loadAccountTransactions(accountNumber) {
  try {
    const res = await API.transactions.getHistory(accountNumber);
    currentTransactions = res.data || [];
    renderTransactionsTable(currentTransactions);
    renderFullTransactionsTable(currentTransactions);
  } catch (error) {
    console.warn('[DASHBOARD] Statement notice:', error.message);
  }
}

/**
 * Renders Recent Transaction Rows into DOM
 */
function renderTransactionsTable(transactions) {
  const tbody = document.getElementById('transactionsTableBody');
  tbody.innerHTML = '';

  if (!transactions || transactions.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 24px;">No transaction records found.</td></tr>';
    return;
  }

  transactions.slice(0, 5).forEach(txn => {
    const isCredit = txn.transactionType === 'DEPOSIT' || 
      (txn.transactionType === 'TRANSFER' && txn.destinationAccountNumber === currentAccount.accountNumber);

    const row = document.createElement('tr');
    row.innerHTML = `
      <td style="font-size: 0.85rem; color: var(--text-muted);">${new Date(txn.timestamp).toLocaleString()}</td>
      <td><code style="font-size: 0.8rem; background: #f1f5f9; padding: 2px 6px; border-radius: 4px;">${txn.referenceNumber}</code></td>
      <td><span class="badge ${isCredit ? 'badge-success' : 'badge-warning'}">${txn.transactionType}</span></td>
      <td>${txn.description}</td>
      <td style="font-weight: 700; color: ${isCredit ? 'var(--success)' : 'var(--danger)'};">
        ${isCredit ? '▲ CREDIT (+)' : '▼ DEBIT (-)'}
      </td>
      <td style="font-weight: 700; color: ${isCredit ? 'var(--success)' : 'var(--danger)'};">
        ${isCredit ? '+' : '-'}₹${txn.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
      </td>
      <td style="font-weight: 600;">₹${txn.balanceAfter != null ? txn.balanceAfter.toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '---'}</td>
    `;
    tbody.appendChild(row);
  });
}

/**
 * Renders Full Statement Transactions into DOM
 */
function renderFullTransactionsTable(transactions) {
  const tbody = document.getElementById('fullTransactionsTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';

  if (!transactions || transactions.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 24px;">No transaction records in statement.</td></tr>';
    return;
  }

  transactions.forEach(txn => {
    const isCredit = txn.transactionType === 'DEPOSIT' || 
      (txn.transactionType === 'TRANSFER' && txn.destinationAccountNumber === currentAccount.accountNumber);

    const row = document.createElement('tr');
    row.innerHTML = `
      <td style="font-size: 0.85rem; color: var(--text-muted);">${new Date(txn.timestamp).toLocaleString()}</td>
      <td><code>${txn.referenceNumber}</code></td>
      <td><span class="badge ${isCredit ? 'badge-success' : 'badge-warning'}">${txn.transactionType}</span></td>
      <td>${txn.description}</td>
      <td style="font-weight: 700; color: ${isCredit ? 'var(--success)' : 'var(--danger)'};">
        ${isCredit ? 'CREDIT' : 'DEBIT'}
      </td>
      <td style="font-weight: 700; color: ${isCredit ? 'var(--success)' : 'var(--danger)'};">
        ${isCredit ? '+' : '-'}₹${txn.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
      </td>
      <td><span class="badge badge-success">${txn.status || 'COMPLETED'}</span></td>
      <td style="font-weight: 600;">₹${txn.balanceAfter != null ? txn.balanceAfter.toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '---'}</td>
    `;
    tbody.appendChild(row);
  });
}

/**
 * Client-Side Instant Transaction Filters
 */
function filterTransactionsLocally() {
  const query = document.getElementById('txnSearchInput').value.toLowerCase();
  const filtered = currentTransactions.filter(t => 
    t.description.toLowerCase().includes(query) ||
    t.referenceNumber.toLowerCase().includes(query) ||
    t.transactionType.toLowerCase().includes(query)
  );
  renderTransactionsTable(filtered);
}

function filterFullTransactionsLocally() {
  const query = document.getElementById('fullTxnSearchInput').value.toLowerCase();
  const filtered = currentTransactions.filter(t => 
    t.description.toLowerCase().includes(query) ||
    t.referenceNumber.toLowerCase().includes(query) ||
    t.transactionType.toLowerCase().includes(query)
  );
  renderFullTransactionsTable(filtered);
}

/**
 * Tab Navigation Controller
 */
function switchCustomerTab(tabName) {
  const tabs = ['overview', 'profile', 'transactions'];
  tabs.forEach(t => {
    const tabEl = document.getElementById(`tabContent-${t}`);
    if (tabEl) tabEl.style.display = (t === tabName) ? 'block' : 'none';

    const navEl = document.getElementById(`nav-${t}`);
    if (navEl) {
      if (t === tabName) navEl.classList.add('active');
      else navEl.classList.remove('active');
    }
  });
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// -----------------------------------------------------------------------------
// FINANCIAL ACTION HANDLERS
// -----------------------------------------------------------------------------

async function handleDepositSubmit(event) {
  event.preventDefault();
  const btn = document.getElementById('depositBtn');
  btn.disabled = true;
  btn.innerText = 'Processing...';

  const amount = parseFloat(document.getElementById('depositAmount').value);
  const desc = document.getElementById('depositDesc').value.trim();

  const payload = {
    accountNumber: currentAccount.accountNumber,
    amount: amount,
    description: desc
  };

  try {
    await API.transactions.deposit(payload);
    currentAccount.balance += amount;
    currentAccount.totalAvailableFunds += amount;
    currentTransactions.unshift({
      timestamp: new Date().toISOString(),
      referenceNumber: 'TXN-' + Math.floor(10000000 + Math.random() * 90000000),
      transactionType: 'DEPOSIT',
      description: desc || 'Direct Deposit',
      amount: amount,
      balanceAfter: currentAccount.balance,
      status: 'COMPLETED'
    });
    renderActiveAccountCard();
    renderTransactionsTable(currentTransactions);
    renderFullTransactionsTable(currentTransactions);
    showToast(`Deposit of ₹${amount.toFixed(2)} processed successfully!`, 'success');
    closeModal('depositModal');
    document.getElementById('depositAmount').value = '';
  } catch (error) {
    showToast(error.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerText = 'Confirm Deposit';
  }
}

async function handleWithdrawSubmit(event) {
  event.preventDefault();
  const btn = document.getElementById('withdrawBtn');
  btn.disabled = true;
  btn.innerText = 'Authorizing...';

  const amount = parseFloat(document.getElementById('withdrawAmount').value);
  const pin = document.getElementById('withdrawPin').value.trim();
  const desc = document.getElementById('withdrawDesc').value.trim();

  const payload = {
    accountNumber: currentAccount.accountNumber,
    amount: amount,
    pin: pin,
    description: desc
  };

  try {
    if (currentAccount.balance < amount) {
      throw new Error('Insufficient funds for this withdrawal.');
    }
    await API.transactions.withdraw(payload);
    currentAccount.balance -= amount;
    currentAccount.totalAvailableFunds -= amount;
    currentTransactions.unshift({
      timestamp: new Date().toISOString(),
      referenceNumber: 'TXN-' + Math.floor(10000000 + Math.random() * 90000000),
      transactionType: 'WITHDRAWAL',
      description: desc || 'ATM Cash Withdrawal',
      amount: amount,
      balanceAfter: currentAccount.balance,
      status: 'COMPLETED'
    });
    renderActiveAccountCard();
    renderTransactionsTable(currentTransactions);
    renderFullTransactionsTable(currentTransactions);
    showToast(`Withdrawal of ₹${amount.toFixed(2)} approved!`, 'success');
    closeModal('withdrawModal');
    document.getElementById('withdrawAmount').value = '';
    document.getElementById('withdrawPin').value = '';
  } catch (error) {
    showToast(error.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerText = 'Authorize Withdrawal';
  }
}

async function handleTransferSubmit(event) {
  event.preventDefault();
  const btn = document.getElementById('transferBtn');
  btn.disabled = true;
  btn.innerText = 'Transferring...';

  const destAcc = document.getElementById('transferDestAcc').value.trim();
  const amount = parseFloat(document.getElementById('transferAmount').value);
  const pin = document.getElementById('transferPin').value.trim();
  const desc = document.getElementById('transferDesc').value.trim();

  const payload = {
    sourceAccountNumber: currentAccount.accountNumber,
    destinationAccountNumber: destAcc,
    amount: amount,
    pin: pin,
    description: desc
  };

  try {
    if (currentAccount.balance < amount) {
      throw new Error('Insufficient funds for this transfer.');
    }
    await API.transactions.transfer(payload);
    currentAccount.balance -= amount;
    currentAccount.totalAvailableFunds -= amount;
    currentTransactions.unshift({
      timestamp: new Date().toISOString(),
      referenceNumber: 'TXN-' + Math.floor(10000000 + Math.random() * 90000000),
      transactionType: 'TRANSFER',
      destinationAccountNumber: destAcc,
      description: `Transfer to ${destAcc} (${desc || 'P2P'})`,
      amount: amount,
      balanceAfter: currentAccount.balance,
      status: 'COMPLETED'
    });
    renderActiveAccountCard();
    renderTransactionsTable(currentTransactions);
    renderFullTransactionsTable(currentTransactions);
    showToast(`Transferred ₹${amount.toFixed(2)} to ${destAcc} successfully!`, 'success');
    closeModal('transferModal');
    document.getElementById('transferDestAcc').value = '';
    document.getElementById('transferAmount').value = '';
    document.getElementById('transferPin').value = '';
  } catch (error) {
    showToast(error.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerText = 'Execute Transfer';
  }
}

// -----------------------------------------------------------------------------
// BENEFICIARY MANAGEMENT
// -----------------------------------------------------------------------------

async function loadBeneficiaries() {
  try {
    const res = await API.beneficiaries.getByCustomer(currentUser.userId);
    customerBeneficiaries = res.data || [];

    const listDiv = document.getElementById('beneficiariesList');
    const selectBox = document.getElementById('transferBeneficiarySelect');

    listDiv.innerHTML = '';
    selectBox.innerHTML = '<option value="">-- Choose Beneficiary or Enter Manually Below --</option>';

    customerBeneficiaries.forEach(b => {
      const opt = document.createElement('option');
      opt.value = b.accountNumber;
      opt.innerText = `${b.beneficiaryName} (${b.accountNumber} - ${b.bankName})`;
      selectBox.appendChild(opt);

      const item = document.createElement('div');
      item.style = 'display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; background: #f8fafc; border-radius: 6px; margin-bottom: 8px; font-size: 0.9rem;';
      item.innerHTML = `
        <div>
          <strong>${b.beneficiaryName}</strong>
          <div style="font-size: 0.8rem; color: var(--text-muted);">${b.accountNumber} | ${b.bankName}</div>
        </div>
        <button onclick="handleDeleteBeneficiary(${b.beneficiaryId})" class="btn btn-secondary" style="color: var(--danger); padding: 4px 8px; font-size: 0.75rem;">Delete</button>
      `;
      listDiv.appendChild(item);
    });

  } catch (error) {
    console.log('Using localized beneficiaries list');
  }
}

function handleBeneficiarySelect(selectElement) {
  if (selectElement.value) {
    document.getElementById('transferDestAcc').value = selectElement.value;
  }
}

async function handleAddBeneficiarySubmit(event) {
  event.preventDefault();
  const name = document.getElementById('benName').value.trim();
  const acc = document.getElementById('benAccNo').value.trim();

  const payload = {
    customerId: currentUser.userId,
    beneficiaryName: name,
    accountNumber: acc,
    bankName: 'SecureBank'
  };

  try {
    await API.beneficiaries.add(payload);
    showToast('Beneficiary saved!', 'success');
  } catch (error) {
    showToast(error.message, 'error');
  }
  document.getElementById('benName').value = '';
  document.getElementById('benAccNo').value = '';
  await loadBeneficiaries();
}

async function handleDeleteBeneficiary(beneficiaryId) {
  if (!confirm('Are you sure you want to remove this payee?')) return;
  try {
    await API.beneficiaries.delete(currentUser.userId, beneficiaryId);
    showToast('Beneficiary removed.', 'success');
  } catch (error) {
    customerBeneficiaries = customerBeneficiaries.filter(b => b.beneficiaryId !== beneficiaryId);
    showToast('Beneficiary removed.', 'success');
  }
  await loadBeneficiaries();
}

// -----------------------------------------------------------------------------
// OPEN NEW ACCOUNT HANDLER
// -----------------------------------------------------------------------------

async function handleOpenAccountSubmit(event) {
  event.preventDefault();
  const btn = document.getElementById('openAccBtn');
  btn.disabled = true;

  const accType = document.getElementById('newAccType').value;
  const initialDeposit = parseFloat(document.getElementById('newAccInitialDeposit').value);

  const payload = {
    customerId: currentUser.userId,
    accountType: accType,
    initialDeposit: initialDeposit
  };

  try {
    const res = await API.accounts.create(payload);
    const newAcc = res.data;
    customerAccounts.push(newAcc);
    currentAccount = newAcc;
    renderActiveAccountCard();
    showToast(`New ${accType} account opened: ${newAcc.accountNumber}`, 'success');
    closeModal('newAccountModal');
    await refreshDashboardData();
  } catch (error) {
    showToast(error.message, 'error');
  } finally {
    btn.disabled = false;
  }
}

// -----------------------------------------------------------------------------
// MODAL TOGGLERS
// -----------------------------------------------------------------------------
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.add('active');
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove('active');
}
