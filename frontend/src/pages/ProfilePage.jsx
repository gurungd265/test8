import { useState, useEffect } from "react";
// import authApi from "../api/auth";
// import userApi from "../api/user";
// import { useAuth } from "../contexts/AuthContext";

export default function ProfilePage() {
    const initialUser = {
        firstName: "John", //
        lastName: "Doe",   //
        firstNameKana: "ジョン", // 名 (カタカナ)
        lastNameKana: "ドゥ",   // 姓 (カタカナ)
        email: "john@mail.com",
        phone: "+1234567890",
        postalCode: "150-0043",
        prefecture: "東京都",
        city: "渋谷区",
        streetAddress: "道玄坂1-2-3",
        // buildingName: "ABCビルディング101",
    };

    const [user, setUser] = useState(initialUser);
    const [editedUser, setEditedUser] = useState(initialUser);
    const [hasChanges, setHasChanges] = useState(false);
    // const { isLoggedIn, logout } = useAuth();
    const [addressSearchError, setAddressSearchError] = useState(null);


    const [isPasswordConfirmed, setIsPasswordConfirmed] = useState(false);
    const [passwordInput, setPasswordInput] = useState("");
    const [passwordError, setPasswordError] = useState(null);


    useEffect(() => {
        const isChanged = Object.keys(user).some(
            (key) => user[key] !== editedUser[key]
        );
        setHasChanges(isChanged);
    }, [editedUser, user]);


    const handleChange = (e) => {
        const { name, value } = e.target;
        setEditedUser((prev) => ({ ...prev, [name]: value }));
    };

    // 저장 버튼 클릭 핸들러
    const handleSave = async () => {
        // 실제 백엔드 연동 시:
        // try {
        //     // userApi.updateUserProfile(editedUser); // 백엔드로 업데이트 요청
        //     // setUser(editedUser); // 성공 시 현재 사용자 정보 업데이트
        //     // setHasChanges(false);
        //     alert("変更を保存しました！");
        // } catch (error) {
        //     console.error("プロフィールの保存に失敗しました。", error);
        //     alert("プロフィールの保存に失敗しました。");
        // }


        setUser(editedUser);
        setHasChanges(false);
        alert("変更を保存しました！");
    };

    const handleLogout = () => {
        // 실제 백엔드 연동 시:
        // logout(); // AuthContext의 로그아웃 함수 호출
        alert("ログアウトしました");
        // 로그인 페이지 등으로 리디렉션 (예: navigate('/login'))
    };


    const handleAddressSearch = async () => {
        const zipcode = editedUser.postalCode.replace(/-/g, '');
        setAddressSearchError(null);

        if (zipcode.length !== 7 || !/^\d+$/.test(zipcode)) {
            setAddressSearchError("有効な7桁の郵便番号を入力してください。");
            return;
        }

        try {
            const response = await fetch(`https://zipcloud.jp/api/search?zipcode=${zipcode}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            if (data.status === 200 && data.results) {
                const result = data.results[0];
                setEditedUser((prev) => ({
                    ...prev,
                    prefecture: result.address1, // 都道府県
                    city: result.address2,       // 市区町村
                    streetAddress: result.address3, // 町域
                }));
            } else if (data.status === 400) {
                setAddressSearchError("無効な郵便番号です。");
            } else {
                setAddressSearchError("住所の検索に失敗しました。もう一度お試しください。");
            }
        } catch (error) {
            console.error("郵便番号検索エラー:", error);
            setAddressSearchError("住所の検索中にエラーが発生しました。ネットワーク接続を確認してください。");
        }
    };

    // ⭐ 추가: 비밀번호 확인 핸들러
    const handlePasswordConfirm = async () => {
        setPasswordError(null);
        // 실제 백엔드 연동 시:
        // try {
        //     // const response = await authApi.confirmPassword(passwordInput);
        //     // if (response.success) { // 백엔드 응답에 따라 조건 변경
        //     //     setIsPasswordConfirmed(true);
        //     // } else {
        //     //     setPasswordError("パスワードが正しくありません。");
        //     // }
        // } catch (error) {
        //     console.error("パスワード確認エラー:", error);
        //     setPasswordError("パスワード確認中にエラーが発生しました。");
        // }

        // 임시 로직: 비밀번호가 "1234"일 경우 성공
        if (passwordInput === "1234") {
            setIsPasswordConfirmed(true);
            setPasswordInput(""); // 비밀번호 입력 필드 초기화
        } else {
            setPasswordError("パスワードが正しくありません。");
        }
    };

    // ⭐ 비밀번호가 확인되지 않았다면 비밀번호 입력 폼을 렌더링
    if (!isPasswordConfirmed) {
        return (
            <div className="max-w-md mx-auto p-6 mt-10 bg-white rounded shadow">
                <h2 className="text-xl font-bold mb-4 text-center">プロフィールの確認</h2>
                <p className="text-gray-600 mb-4 text-center">
                    プロフィール情報を変更するには、パスワードを入力してください。
                </p>
                <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700" htmlFor="confirmPassword">パスワード</label>
                    <input
                        type="password"
                        id="confirmPassword"
                        value={passwordInput}
                        onChange={(e) => setPasswordInput(e.target.value)}
                        className="w-full mt-1 border rounded px-3 py-2 focus:ring-purple-500 focus:border-purple-500"
                        placeholder="パスワードを入力してください"
                    />
                    {passwordError && (
                        <p className="text-red-500 text-sm mt-1">{passwordError}</p>
                    )}
                </div>
                <button
                    onClick={handlePasswordConfirm}
                    className="w-full bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-opacity-50"
                >
                    確認
                </button>
            </div>
        );
    }


    return (
        <div className="max-w-xl mx-auto p-6">
            <h1 className="text-2xl font-bold mb-6">マイプロフィール</h1>

            <div className="bg-white rounded shadow p-6 space-y-4">
                {/* 氏名 (英字) */}
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">名 (英字)</label>
                        <input
                            type="text"
                            name="firstName"
                            value={editedUser.firstName}
                            onChange={handleChange}
                            className="w-full mt-1 border rounded px-3 py-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">姓 (英字)</label>
                        <input
                            type="text"
                            name="lastName"
                            value={editedUser.lastName}
                            onChange={handleChange}
                            className="w-full mt-1 border rounded px-3 py-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                </div>

                {/* 氏名 (カタカナ) */}
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">名 (カタカナ)</label>
                        <input
                            type="text"
                            name="firstNameKana"
                            value={editedUser.firstNameKana}
                            onChange={handleChange}
                            className="w-full mt-1 border rounded px-3 py-2 focus:ring-purple-500 focus:border-purple-500"
                            placeholder="例: ジョン"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">姓 (カタカナ)</label>
                        <input
                            type="text"
                            name="lastNameKana"
                            value={editedUser.lastNameKana}
                            onChange={handleChange}
                            className="w-full mt-1 border rounded px-3 py-2 focus:ring-purple-500 focus:border-purple-500"
                            placeholder="例: ドゥ"
                        />
                    </div>
                </div>

                {/* メールアドレス */}
                <div>
                    <label className="block text-sm font-medium text-gray-700">メールアドレス</label>
                    <input
                        type="email"
                        name="email"
                        value={editedUser.email}
                        onChange={handleChange}
                        className="w-full mt-1 border rounded px-3 py-2 focus:ring-purple-500 focus:border-purple-500"
                    />
                </div>

                {/* 電話番号 */}
                <div>
                    <label className="block text-sm font-medium text-gray-700">電話番号</label>
                    <input
                        type="tel"
                        name="phone"
                        value={editedUser.phone}
                        placeholder="080-000-0000"
                        onChange={handleChange}
                        className="w-full mt-1 border rounded px-3 py-2 focus:ring-purple-500 focus:border-purple-500"
                    />
                </div>

                {/* 住所 (日本式) */}
                <div className="space-y-4 pt-4 border-t border-gray-200">
                    <h2 className="text-lg font-semibold text-gray-800">お届け先住所</h2>
                        <div className="flex items-end gap-2">
                            <div className="flex-grow">
                                <label className="block text-sm font-medium text-gray-700" htmlFor="postalCode">郵便番号 (必須)</label>
                                <input
                                    type="text"
                                    id="postalCode"
                                    name="postalCode"
                                    value={editedUser.postalCode}
                                    onChange={handleChange}
                                    className="w-full mt-1 border rounded px-3 py-2 focus:ring-purple-500 focus:border-purple-500"
                                    placeholder="例: 150-0043"
                                    maxLength="8"
                                    required
                                />
                            </div>
                            <button
                                type="button"
                                onClick={handleAddressSearch}
                                className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50"
                                >
                                検索
                            </button>
                        </div>
                            {addressSearchError && ( // アドレス検索エラーメッセージ表示
                            <p className="text-red-500 text-sm mt-1">{addressSearchError}</p>
                            )}
                            <div>
                                <label className="block text-sm font-medium text-gray-700">都道府県 (必須)</label>
                                <input
                                    type="text"
                                    name="prefecture"
                                    value={editedUser.prefecture}
                                    onChange={handleChange}
                                    className="w-full mt-1 border rounded px-3 py-2 bg-gray-50 focus:ring-purple-500 focus:border-purple-500"
                                    placeholder="例: 東京都"
                                    readOnly
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700">市区町村 (必須)</label>
                                    <input
                                        type="text"
                                        name="city"
                                        value={editedUser.city}
                                        onChange={handleChange}
                                        className="w-full mt-1 border rounded px-3 py-2 bg-gray-50 focus:ring-purple-500 focus:border-purple-500"
                                        placeholder="例: 渋谷区"
                                        readOnly
                                        required
                                    />
                            </div>
                            {/* streetAddress */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700">番地・建物名など (必須)</label>
                                <input
                                    type="text"
                                    name="streetAddress"
                                    value={editedUser.streetAddress}
                                    onChange={handleChange}
                                    className="w-full mt-1 border rounded px-3 py-2 focus:ring-purple-500 focus:border-purple-500"
                                    placeholder="例: 道玄坂1-2-3 ABCビルディング101"
                                    required
                                />
                            </div>
                </div>
                {/* Button */}
                <div className="flex justify-between mt-6">
                    {hasChanges && (
                        <button
                            onClick={handleSave}
                            className="px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-opacity-50"
                        >
                            変更を保存する
                        </button>
                    )}
                    <button
                        onClick={handleLogout}
                        className="px-4 py-2 border border-red-500 text-red-500 rounded hover:bg-red-100 ml-auto focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-opacity-50"
                    >
                        ログアウト
                    </button>
                </div>
            </div>
        </div>
    );
}