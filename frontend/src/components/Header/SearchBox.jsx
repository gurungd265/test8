import React from "react";
import { Search } from "lucide-react";

export default function SearchBox({ searchTerm, setSearchTerm, onSearch, onKeyPress }) {
    return (
        <div className="hidden lg:flex items-center border rounded overflow-hidden bg-gray-50" style={{ height: '32px' }}>
            <input
                type="search"
                placeholder="探す..."
                className="px-2 py-0 outline-none bg-gray-50 text-sm"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyDown={onKeyPress}
                aria-label="Search products"
                style={{ height: '32px' }}
            />
            <button
                className="px-2 bg-purple-600 text-white flex items-center justify-center"
                onClick={onSearch}
                aria-label="Submit search"
                style={{ height: '32px' }}
            >
                <Search size={14} />
            </button>
        </div>
    );
}