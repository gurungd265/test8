import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';

import paymentsApi from '../api/payments';

export default function CheckoutPage() {
    // 위치에서 상태 받아오기
    const location = useLocation();
    const cart = location.state?.cart;

    console.log('CheckoutPage cart:', cart);
    console.log('cart.items:', cart.items);

    if (!cart) {
        return <div>カートの情報がありません。</div>;
    }

    // Constants
    const JAPANESE_PREFECTURES = [
        '北海道', '青森県', '岩手県', '宮城県', '秋田県',
        '山形県', '福島県', '茨城県', '栃木県', '群馬県',
        '埼玉県', '千葉県', '東京都', '神奈川県', '新潟県',
        '富山県', '石川県', '福井県', '山梨県', '長野県',
        '岐阜県', '静岡県', '愛知県', '三重県', '滋賀県',
        '京都府', '大阪府', '兵庫県', '奈良県', '和歌山県',
        '鳥取県', '島根県', '岡山県', '広島県', '山口県',
        '徳島県', '香川県', '愛媛県', '高知県', '福岡県',
        '佐賀県', '長崎県', '熊本県', '大分県', '宮崎県',
        '鹿児島県', '沖縄県'
    ];

    const DELIVERY_TIME_SLOTS = [
        { value: 'morning', label: '午前中 (8-12時)' },
        { value: 'afternoon', label: '14-16時' },
        { value: 'evening', label: '16-18時' },
        { value: 'night', label: '18-21時' }
    ];

    // Get cart items from location state or use empty array as fallback
    const cartItems = location.state?.cartItems || [];
    const subtotalFromCart = location.state?.subtotal || 0;

    // State
    const [formData, setFormData] = useState({
        name: '',
        nameKana: '',
        postalCode: '',
        prefecture: '',
        city: '',
        address: '',
        building: '',
        phone: '',
        email: '',
        paymentMethod: 'credit_card',
        deliveryDate: '',
        deliveryTime: ''
    });

    const [deliveryDates, setDeliveryDates] = useState([]);
    const [isLoading, setIsLoading] = useState(false);

    // Calculate totals
    const subtotal = subtotalFromCart || cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const shippingFee = 600;
    const tax = Math.floor(subtotal * 0.1);
    const total = subtotal + shippingFee + tax;

    // Generate delivery dates on mount
    useEffect(() => {
        const dates = [];
        const today = new Date();

        for (let i = 2; i <= 7; i++) {
            const date = new Date(today);
            date.setDate(today.getDate() + i);

            if (date.getDay() !== 0) { // Skip Sundays
                dates.push({
                    value: date.toISOString().split('T')[0],
                    label: `${date.getMonth()+1}月${date.getDate()}日 (${['月','火','水','木','金','土'][date.getDay()-1]})`
                });
            }
        }

        setDeliveryDates(dates);
    }, []);

    // Handlers
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handlePostalCodeLookup = async () => {
        if (formData.postalCode.replace('-', '').length !== 7) return;

        setIsLoading(true);
        try {
            // Mock API call
            setTimeout(() => {
                setFormData(prev => ({
                    ...prev,
                    prefecture: '東京都',
                    city: '渋谷区',
                    address: '道玄坂1丁目'
                }));
                setIsLoading(false);
            }, 1000);
        } catch (error) {
            setIsLoading(false);
            alert('住所の取得に失敗しました');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            // 결제 요청 데이터 만들기
            const paymentRequest = {
                orderId: generateOrderId(),  // 주문 ID 생성 함수 또는 실제 주문 ID
                paymentMethod: formData.paymentMethod, // formData에 결제수단 정보가 있다면
                amount: total,
                transactionId: generateTransactionId(), // 고유 트랜잭션 ID 생성 함수
            };

            // 결제 API 호출
            const paymentResponse = await paymentsApi.createPayment(paymentRequest);

            console.log('決済失敗:', paymentResponse);

            alert('注文が確定しました！ 결제가 완료되었습니다.');

            // 주문 및 결제 성공 후 추가 작업 (페이지 이동, 상태 초기화 등)

        } catch (error) {
            console.error('決済失敗:', error);
            alert('決済に失敗しました。もう一度お試しください。');
        }
    };

    return (
        <div className="container mx-auto px-4 py-8 max-w-6xl">
            <h1 className="text-2xl font-bold mb-8 text-gray-800 border-b pb-4">注文内容の確認</h1>

            <div className="flex flex-col lg:flex-row gap-8">
                <form className="lg:w-2/3" onSubmit={handleSubmit}>
                    {/* Customer Information */}
                    <section className="bg-white p-6 rounded-lg shadow-md mb-6">
                        <h2 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">お客様情報</h2>

                        <div className="mb-4">
                            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">お名前（漢字）</label>
                            <input
                                type="text"
                                id="name"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                required
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div className="mb-4">
                            <label htmlFor="nameKana" className="block text-sm font-medium text-gray-700 mb-1">お名前（フリガナ）</label>
                            <input
                                type="text"
                                id="nameKana"
                                name="nameKana"
                                value={formData.nameKana}
                                onChange={handleChange}
                                required
                                pattern="[\u30A1-\u30FC]+"
                                title="全角カタカナで入力してください"
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>

                        <div className="mb-4">
                            <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-1">電話番号</label>
                            <input
                                type="tel"
                                id="phone"
                                name="phone"
                                value={formData.phone}
                                onChange={handleChange}
                                required
                                pattern="\d{2,4}-?\d{2,4}-?\d{3,4}"
                                placeholder="090-1234-5678"
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>

                        <div className="mb-4">
                            <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">メールアドレス</label>
                            <input
                                type="email"
                                id="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                required
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>

                        {/* ... (rest of the form sections remain the same) ... */}
                    </section>
                    <section className="bg-white p-6 rounded-lg shadow-md mb-6">
                        <h2 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">お届け先情報</h2>

                        <div className="mb-4">
                            <label htmlFor="postalCode" className="block text-sm font-medium text-gray-700 mb-1">郵便番号</label>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    id="postalCode"
                                    name="postalCode"
                                    value={formData.postalCode}
                                    onChange={handleChange}
                                    placeholder="123-4567"
                                    pattern="\d{3}-\d{4}"
                                    className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                                <button
                                    type="button"
                                    className="bg-gray-200 hover:bg-gray-300 text-gray-800 px-4 py-2 rounded-md transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                    onClick={handlePostalCodeLookup}
                                    disabled={!formData.postalCode || formData.postalCode.replace('-', '').length !== 7 || isLoading}
                                >
                                    {isLoading ? '検索中...' : '住所自動入力'}
                                </button>
                            </div>
                        </div>

                        <div className="mb-4">
                            <label htmlFor="prefecture" className="block text-sm font-medium text-gray-700 mb-1">都道府県</label>
                            <select
                                id="prefecture"
                                name="prefecture"
                                value={formData.prefecture}
                                onChange={handleChange}
                                required
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="">選択してください</option>
                                {JAPANESE_PREFECTURES.map(pref => (
                                    <option key={pref} value={pref}>{pref}</option>
                                ))}
                            </select>
                        </div>

                        <div className="mb-4">
                            <label htmlFor="city" className="block text-sm font-medium text-gray-700 mb-1">市区町村</label>
                            <input
                                type="text"
                                id="city"
                                name="city"
                                value={formData.city}
                                onChange={handleChange}
                                required
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>

                        <div className="mb-4">
                            <label htmlFor="address" className="block text-sm font-medium text-gray-700 mb-1">番地</label>
                            <input
                                type="text"
                                id="address"
                                name="address"
                                value={formData.address}
                                onChange={handleChange}
                                required
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>

                        <div className="mb-4">
                            <label htmlFor="building" className="block text-sm font-medium text-gray-700 mb-1">建物名・部屋番号</label>
                            <input
                                type="text"
                                id="building"
                                name="building"
                                value={formData.building}
                                onChange={handleChange}
                                placeholder="マンション・アパート名など"
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                    </section>

                    {/* Delivery Options */}
                    <section className="bg-white p-6 rounded-lg shadow-md mb-6">
                        <h2 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">配送オプション</h2>

                        <div className="mb-4">
                            <label htmlFor="deliveryDate" className="block text-sm font-medium text-gray-700 mb-1">お届け日</label>
                            <select
                                id="deliveryDate"
                                name="deliveryDate"
                                value={formData.deliveryDate}
                                onChange={handleChange}
                                required
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="">選択してください</option>
                                {deliveryDates.map(date => (
                                    <option key={date.value} value={date.value}>{date.label}</option>
                                ))}
                            </select>
                        </div>

                        <div className="mb-4">
                            <label htmlFor="deliveryTime" className="block text-sm font-medium text-gray-700 mb-1">お届け時間帯</label>
                            <select
                                id="deliveryTime"
                                name="deliveryTime"
                                value={formData.deliveryTime}
                                onChange={handleChange}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="">指定なし</option>
                                {DELIVERY_TIME_SLOTS.map(slot => (
                                    <option key={slot.value} value={slot.value}>{slot.label}</option>
                                ))}
                            </select>
                        </div>
                    </section>

                    {/* Payment Methods */}
                    <section className="bg-white p-6 rounded-lg shadow-md mb-6">
                        <h2 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">お支払い方法</h2>

                        <div className="space-y-3 mb-6">
                            <label className="flex items-center space-x-2">
                                <input
                                    type="radio"
                                    name="paymentMethod"
                                    value="credit_card"
                                    checked={formData.paymentMethod === 'credit_card'}
                                    onChange={handleChange}
                                    className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                                />
                                <span className="text-gray-700">クレジットカード</span>
                            </label>

                            <label className="flex items-center space-x-2">
                                <input
                                    type="radio"
                                    name="paymentMethod"
                                    value="konbini"
                                    checked={formData.paymentMethod === 'konbini'}
                                    onChange={handleChange}
                                    className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                                />
                                <span className="text-gray-700">コンビニ払い</span>
                            </label>

                            <label className="flex items-center space-x-2">
                                <input
                                    type="radio"
                                    name="paymentMethod"
                                    value="bank_transfer"
                                    checked={formData.paymentMethod === 'bank_transfer'}
                                    onChange={handleChange}
                                    className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                                />
                                <span className="text-gray-700">銀行振込</span>
                            </label>

                            <label className="flex items-center space-x-2">
                                <input
                                    type="radio"
                                    name="paymentMethod"
                                    value="cod"
                                    checked={formData.paymentMethod === 'cod'}
                                    onChange={handleChange}
                                    className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                                />
                                <span className="text-gray-700">代金引換</span>
                            </label>
                        </div>

                        {formData.paymentMethod === 'credit_card' && (
                            <div className="space-y-4 bg-gray-50 p-4 rounded-md">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">カード番号</label>
                                    <input
                                        type="text"
                                        placeholder="1234 5678 9012 3456"
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    />
                                </div>
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">有効期限</label>
                                        <input
                                            type="text"
                                            placeholder="MM/YY"
                                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">セキュリティコード</label>
                                        <input
                                            type="text"
                                            placeholder="CVC"
                                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        />
                                    </div>
                                </div>
                            </div>
                        )}

                        {formData.paymentMethod === 'konbini' && (
                            <div className="bg-gray-50 p-4 rounded-md text-sm text-gray-600">
                                <p>コンビニ選択と支払い番号は注文確定後にお知らせします</p>
                            </div>
                        )}
                    </section>


                    {/* ... (other form sections remain unchanged) ... */}

                    <button
                        type="submit"
                        className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                        disabled={cartItems.length === 0}
                    >
                        注文を確定する
                    </button>
                </form>

                {/* Order Summary */}
                <div className="lg:w-1/3">
                    <div className="bg-white p-6 rounded-lg shadow-md sticky top-4">
                        <h2 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">注文内容</h2>

                        {cartItems.length === 0 ? (
                            <div className="text-gray-500 text-center py-4">カートに商品がありません</div>
                        ) : (
                            <>
                                <div className="space-y-4 mb-6">
                                    {cartItems.map((item) => (
                                        <div key={item.id} className="flex gap-4">
                                            <div className="flex-shrink-0">
                                                <div className="h-16 w-16 bg-gray-200 rounded-md flex items-center justify-center overflow-hidden">
                                                    <img
                                                        src={item.image}
                                                        alt={item.name}
                                                        className="w-full h-full object-cover"
                                                    />
                                                </div>
                                            </div>
                                            <div className="flex-1">
                                                <h3 className="font-medium text-gray-800">{item.name}</h3>
                                                <p className="text-sm text-gray-600">¥{item.price.toLocaleString()} × {item.quantity}</p>
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                <div className="space-y-2 mb-6">
                                    <div className="flex justify-between">
                                        <span className="text-gray-600">小計</span>
                                        <span className="text-gray-800">¥{subtotal.toLocaleString()}</span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span className="text-gray-600">配送料</span>
                                        <span className="text-gray-800">¥{shippingFee.toLocaleString()}</span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span className="text-gray-600">消費税</span>
                                        <span className="text-gray-800">¥{tax.toLocaleString()}</span>
                                    </div>
                                    <div className="flex justify-between border-t pt-2 mt-2">
                                        <span className="font-semibold text-gray-800">合計</span>
                                        <span className="font-bold text-gray-900">¥{total.toLocaleString()}</span>
                                    </div>
                                </div>
                            </>
                        )}

                        <div className="text-xs text-gray-500 space-y-2">
                            <p>ご注文内容の確認後、注文確定ボタンを押してください。</p>
                            <p>当サイトの<a href="/privacy" className="text-blue-600 hover:underline">プライバシーポリシー</a>に同意の上、お進みください。</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};