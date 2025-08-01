import React, { useEffect, useState, useContext } from "react";
import { MapPin } from "lucide-react";
import { AuthContext } from "../../contexts/AuthContext.jsx";
import { getCurrentLocation } from "../../utils/locationAPI";
import { fetchJapanesePostalCode } from "../../utils/postcodeAPI";
import AddressSectionWrapper from "../Profile/AddressSectionWrapper.jsx";
import Modal from "../Modal";

export default function LocationButton() {
    const { user, defaultAddress, setDefaultAddress } = useContext(AuthContext);
    const [postalCode, setPostalCode] = useState("...");
    const [isModalOpen, setIsModalOpen] = useState(false);

    useEffect(() => {
        async function updatePostalCode() {
            if (user && defaultAddress) {
                setPostalCode(defaultAddress.postalCode);
            } else {
                try {
                    const { latitude, longitude } = await getCurrentLocation();
                    const code = await fetchJapanesePostalCode(latitude, longitude);
                    setPostalCode(code || "Unknown");
                } catch (error) {
                    setPostalCode("Unavailable");
                }
            }
        }
        updatePostalCode();
    }, [user, defaultAddress]);

    return (
        <>
            <button
                className="flex flex-col items-start text-xs text-gray-700"
                aria-label="Location"
                onClick={() => setIsModalOpen(true)}
            >
                <div className="flex items-center gap-1">
                    <MapPin size={14} />
                    〒{postalCode}
                </div>
                <span className="mt-1">お届け先を変更する</span>
            </button>

            {isModalOpen && (
                <Modal onClose={() => setIsModalOpen(false)}>
                    <AddressSectionWrapper
                        onClose={() => setIsModalOpen(false)}
                        setDefaultAddress={setDefaultAddress}
                    />
                </Modal>
            )}
        </>
    );
}
