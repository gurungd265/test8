import { useState, useEffect } from "react";

export default function ProfilePage() {
    const initialUser = {
        first_name: "John",
        last_name: "Doe",
        email: "john@mail.com",
        phone: "+1234567890",
        address: "Japan, Tokyo, Shibuya, 123-4567",
    }

    const [user, setUser] = useState(initialUser);
    const [editedUser, setEditedUser] = useState(initialUser);
    const [hasChanges, setHasChanges] = useState(false);

    useEffect(() => {
        const isChanged = Object.keys(user).some(
            (key) => user[key] !== editedUser[key]
        );
        setHasChanges(isChanged);
    }, [editedUser, user]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setEditedUser((prev) => ({...prev, [name]: value}));
    };

    const handleSave = () => {
        setUser(editedUser);
        setHasChanges(false);
        alert("変更を保存しました！");
    };

    const handleLogout = () => {
        alert("ログアウトしました");
    };

    return(
        <div className="max-w-xl mx-auto p-6">
            <h1 className="text-2xl font-bold mb-6">マイプロフィール</h1>

            <div className="bg-white rounded shadow p-6 space-y-4">
                <div>
                    <label className="block text-sm font-medium text-gray-500">名</label>
                    <input 
                        type="text" 
                        name="first_name"
                        value={editedUser.first_name}
                        onChange={handleChange}
                        className="w-full mt-1 border rounded px-3 py-2"
                    />
                </div>
                
                <div>
                    <label className="block text-sm font-medium text-gray-500">姓</label>
                    <input 
                        type="text" 
                        name="last_name"
                        value={editedUser.last_name}
                        onChange={handleChange}
                        className="w-full mt-1 border rounded px-3 py-2"
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-500">メール</label>
                    <input 
                        type="email" 
                        name="email"
                        value={editedUser.email}
                        onChange={handleChange}
                        className="w-full mt-1 border rounded px-3 py-2"
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-500">電話番号</label>
                    <input 
                        type="tel" 
                        name="phone"
                        value={editedUser.phone}
                        onChange={handleChange}
                        className="w-full mt-1 border rounded px-3 py-2"
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-500">お届け先</label>
                    <input 
                        type="text" 
                        name="address"
                        value={editedUser.address}
                        onChange={handleChange}
                        className="w-full mt-1 border rounded px-3 py-2"
                    />
                </div>

                <div className="flex justify-between mt-6">
                    {hasChanges && (
                        <button 
                            onClick={handleSave}
                            className="px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700"
                        >
                            保存する
                        </button>
                    )}
                    <button
                        onClick={handleLogout}
                        className="px-4 py-2 border border-red-500 text-red-500 rounded hover:bg-red-100 ml-auto"
                    >
                        ログアウト
                    </button>
                </div>
            </div>
        </div>
    )
}