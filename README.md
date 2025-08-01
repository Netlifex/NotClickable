# NotClickable

<img width="2500" height="2500" alt="cb63c597-74e3-449b-a92f-95dd4b3493a1" src="https://github.com/user-attachments/assets/73ad88ab-a31c-47c9-a958-23076c0fe461" />


**NotClickable** is a Minecraft Forge mod for 1.20.1 that lets you mark blocks as unclickable, preventing players from interacting with them.

It’s useful for:
- Adventure maps
- Cutscenes
- Server protection
- Puzzle/command-based builds

---

## 🔧 Features

- ❌ Prevents right-click interactions on marked blocks  
- ✅ Use `/notclickable <pos> <true|false>` to mark/unmark any block  
- 🧭 Supports selecting blocks you're looking at  
- 📋 `/notclickable list` shows all protected blocks in your world  
- ✨ Blocks in the list are highlighted with a glowing cyan particle outline (5 seconds)

---

## 📦 Commands

```bash
/notclickable <x y z> true     # mark a block as not clickable
/notclickable <x y z> false    # unmark a block
/notclickable true             # mark the block you're looking at
/notclickable false            # unmark the block you're looking at
/notclickable list             # show all marked blocks with clickable teleport messages
