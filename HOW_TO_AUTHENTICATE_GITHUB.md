# 🔐 How to Authenticate with GitHub

## 🎯 Quick Answer: Use Personal Access Token

GitHub requires authentication to push code. Here's the easiest way:

---

## 📋 Step-by-Step Authentication Guide

### **Method 1: Personal Access Token (Recommended - Easiest)**

#### **Step 1: Create Personal Access Token**

1. **Go to GitHub Settings:**
   - Open: https://github.com/settings/tokens
   - Or: GitHub → Your Profile (top right) → Settings → Developer settings → Personal access tokens → Tokens (classic)

2. **Generate New Token:**
   - Click **"Generate new token"**
   - Select **"Generate new token (classic)"**

3. **Configure Token:**
   - **Note:** `RegexFlow Push Token` (or any name you like)
   - **Expiration:** Choose:
     - `90 days` (recommended for security)
     - `No expiration` (convenient but less secure)
   - **Select scopes:** 
     - ✅ Check **`repo`** (Full control of private repositories)
     - This gives you permission to push/pull

4. **Generate:**
   - Scroll down and click **"Generate token"**

5. **⚠️ COPY THE TOKEN IMMEDIATELY!**
   - It looks like: `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`
   - You won't see it again!
   - Save it somewhere safe (password manager, notes app, etc.)

#### **Step 2: Use Token to Push**

1. **Open Terminal:**
   ```bash
   cd /Users/rekanksha.malikireddy/RegFlow/RegexFlow
   ```

2. **Push Your Code:**
   ```bash
   git push origin main
   ```

3. **When Prompted:**
   - **Username:** Enter your GitHub username (e.g., `vedant-dewangan`)
   - **Password:** Paste the token you copied (NOT your GitHub password!)

4. **Done!** Your code will push to GitHub.

#### **Step 3: Save Credentials (Optional - So You Don't Have to Enter Token Every Time)**

**For macOS:**
```bash
git config --global credential.helper osxkeychain
```

**For Linux:**
```bash
git config --global credential.helper store
```

**For Windows:**
```bash
git config --global credential.helper wincred
```

After setting this, the first time you push, macOS will save your credentials. Next time, you won't need to enter them again!

---

## 🔑 Method 2: SSH Keys (Alternative - More Secure Long-term)

### **Step 1: Check if You Already Have SSH Key**

```bash
ls -la ~/.ssh
```

Look for files like:
- `id_rsa` and `id_rsa.pub` (RSA key)
- `id_ed25519` and `id_ed25519.pub` (Ed25519 key - newer, recommended)

### **Step 2: Generate SSH Key (If You Don't Have One)**

```bash
ssh-keygen -t ed25519 -C "your.email@example.com"
```

**When prompted:**
- **File location:** Press Enter (uses default: `~/.ssh/id_ed25519`)
- **Passphrase:** Enter a password (optional but recommended) or press Enter for no passphrase

### **Step 3: Add SSH Key to GitHub**

1. **Copy Your Public Key:**
   ```bash
   cat ~/.ssh/id_ed25519.pub
   ```
   (Or `cat ~/.ssh/id_rsa.pub` if you used RSA)

2. **Copy the entire output** (starts with `ssh-ed25519` or `ssh-rsa`)

3. **Add to GitHub:**
   - Go to: https://github.com/settings/keys
   - Click **"New SSH key"**
   - **Title:** `My Mac` (or any name)
   - **Key:** Paste the public key you copied
   - Click **"Add SSH key"**

### **Step 4: Test SSH Connection**

```bash
ssh -T git@github.com
```

You should see: `Hi username! You've successfully authenticated...`

### **Step 5: Change Remote to SSH**

```bash
cd /Users/rekanksha.malikireddy/RegFlow/RegexFlow
git remote set-url origin git@github.com:vedant-dewangan/RegexFlow.git
```

### **Step 6: Push**

```bash
git push origin main
```

No password needed! SSH handles authentication automatically.

---

## 🎯 Method 3: GitHub CLI (If You Have It Installed)

### **Step 1: Install GitHub CLI (If Not Installed)**

**macOS:**
```bash
brew install gh
```

**Or download from:** https://cli.github.com/

### **Step 2: Authenticate**

```bash
gh auth login
```

Follow the prompts:
- Choose GitHub.com
- Choose HTTPS or SSH
- Authenticate in browser

### **Step 3: Push**

```bash
cd /Users/rekanksha.malikireddy/RegFlow/RegexFlow
git push origin main
```

---

## ✅ Recommended: Personal Access Token

**Why?**
- ✅ Easiest to set up (5 minutes)
- ✅ Works immediately
- ✅ No SSH key management
- ✅ Can be revoked anytime from GitHub settings

**Steps:**
1. Create token: https://github.com/settings/tokens
2. Select `repo` scope
3. Copy token
4. Use as password when pushing

---

## 🔍 Verify Authentication Worked

After pushing, check:

```bash
git status
```

Should show: `Your branch is up to date with 'origin/main'`

Or check GitHub: https://github.com/vedant-dewangan/RegexFlow

---

## 🐛 Troubleshooting

### **Problem: "fatal: could not read Username"**

**Solution:** Make sure you're entering:
- Username: Your GitHub username
- Password: The token (not your actual password)

---

### **Problem: "Permission denied"**

**Solution:** 
- Check token has `repo` scope
- Make sure token hasn't expired
- Generate a new token if needed

---

### **Problem: "remote: Support for password authentication was removed"**

**Solution:** You MUST use Personal Access Token, not your GitHub password

---

### **Problem: Token Not Saved**

**Solution:** 
```bash
# For macOS
git config --global credential.helper osxkeychain

# Then push again - it will save this time
git push origin main
```

---

## 📝 Quick Reference

| Method | Setup Time | Security | Convenience |
|--------|------------|----------|-------------|
| **Personal Access Token** | 5 min | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **SSH Keys** | 10 min | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **GitHub CLI** | 5 min | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

---

## 🚀 Quick Start (Copy & Paste)

**Easiest way - Personal Access Token:**

1. **Get token:** https://github.com/settings/tokens
   - Generate new token (classic)
   - Select `repo` scope
   - Copy token

2. **Push:**
   ```bash
   cd /Users/rekanksha.malikireddy/RegFlow/RegexFlow
   git push origin main
   ```

3. **Enter:**
   - Username: Your GitHub username
   - Password: Paste the token

4. **Save credentials (optional):**
   ```bash
   git config --global credential.helper osxkeychain
   ```

**Done!** 🎉

---

## 💡 Pro Tips

1. **Save token securely:** Use a password manager
2. **Token expiration:** Set a reminder before it expires
3. **Revoke old tokens:** If you lose a token, revoke it and create a new one
4. **One token, many repos:** One token works for all your repositories
5. **Use SSH for long-term:** More secure and convenient once set up

---

## ✅ Summary

**To authenticate with GitHub:**

1. **Get Personal Access Token** (easiest):
   - Go to: https://github.com/settings/tokens
   - Generate token with `repo` scope
   - Copy token

2. **Push your code:**
   ```bash
   git push origin main
   ```

3. **Enter credentials:**
   - Username: Your GitHub username
   - Password: The token

4. **Save credentials (optional):**
   ```bash
   git config --global credential.helper osxkeychain
   ```

**That's it! You're authenticated!** 🎉
