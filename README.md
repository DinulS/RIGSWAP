RIGSWAP 🖥️⚡


RIGSWAP is a specialized Android application designed for PC builders, gamers, and hardware enthusiasts in Sri Lanka. It serves as a dedicated peer-to-peer marketplace for buying, selling, and swapping PC components (GPUs, CPUs, Cooling, Storage, PSUs, and Peripherals) with built-in hardware compatibility validation against saved user rig specifications.

✨ Features


🛍️ PC Hardware Marketplace

Category Browsing: Filter components by GPUs, CPUs, Cooling, Storage, PSUs, and Peripherals using interactive Material Design 3 category chips.
Search: Instant keyword search across titles, brands, and hardware categories.
LKR Currency Formatting: All listing prices and total calculations formatted in Sri Lankan Rupees (LKR).
Condition Badges: Clear condition status tags (NEW, USED, REFURB).
Product Details: Detailed view with multi-image carousel slider, full technical specifications (TDP, Socket, Power Connectors, Memory, Suggested PSU), seller rating, and location.
⚡ Hardware Compatibility Engine

Saved Rig Specification: Store current system hardware specs (e.g., Motherboard Socket AM5, DDR5 RAM, PCIe Gen 5.0 x16 slot, 850W PSU, System Health Score).
Automatic Compatibility Checking: Validates component specifications (Socket types, RAM standards, TDP, PSU requirements) against saved hardware specs.
"Compatible Parts" Filter: One-tap filtering view displaying only components guaranteed to fit your build.
🛒 E-Commerce & Shopping Cart

Shopping Cart Management: Add components to cart, adjust item quantities, remove items, and calculate total price dynamically in LKR.
💼 Merchant Hub & Seller Tools

Seller Dashboard: Monitor active listing counts and total sales volume (in LKR).
Order & Transaction Tracking: Real-time tracking of order statuses (PENDING, SHIPPED, DELIVERED).
Create Listing Wizard: Intuitive form layout allowing sellers to publish new hardware listings complete with custom technical specifications.
💬 Buyer-Seller Messaging

In-App Direct Chat: Direct messaging between buyers and sellers to discuss component details, price negotiation, and pickup arrangements.
🛠️ Architecture & Tech Stack

Language: 100% Kotlin
Minimum SDK: 24 (Android 7.0 Nougat)
Target SDK / Compile SDK: 37
UI & Layouts: Material Design 3, ViewBinding, RecyclerView, ViewPager2, custom chip adapters
Architecture: Repository Pattern (RigswapRepository) separating data, database helper, and UI layer
Database & Persistence: SQLite (SQLiteOpenHelper via RigswapDbHelper) pre-seeded with hardware inventory, user profiles, saved rigs, transactions, and chat messages
