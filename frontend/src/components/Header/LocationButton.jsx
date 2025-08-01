import React from "react";
import { MapPin } from "lucide-react";

export default function LocationButton({ locationName = "Tokyo" }) {
    return (
        <button
            className="flex items-center gap-1 text-xs text-gray-700"
            aria-label="Location"
        >
            <MapPin size={14} />
            {locationName}
        </button>
    );
}
