import React from "react";

export default function CategoryFilter({ categories, selectedCategory, onFilter }) {
    return (
        <div className={"mb-6"}>
            <h3 className="font-medium text-gray-700 mb-2">Categories</h3>
            <div className="space-y-2">
                <button
                    onClick={() => onFilter(null)}
                    className={`w-full text-left px-3 py-2 rounded-md text-sm ${
                        !selectedCategory ? "bg-blue-100 text-blue-800" : "hover:bg-gray-100"
                    }`}
                >
                    All Categories
                </button>

                {categories.map((category) => (
                    <button
                        key={category.id}
                        onClick={() => onFilter(category.id)}
                        className={`w-full text-left px-3 py-2 rounded-md text-sm ${
                            selectedCategory === category.id ? "bg-blue-100 text-blue-800" : "hover:bg-gray-100"
                        }`}
                    >
                        {category.name}
                    </button>
                ))}
            </div>
        </div>
    );
}