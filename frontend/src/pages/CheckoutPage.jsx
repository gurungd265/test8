import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import orderApi from '../api/order';
import paymentsApi from '../api/payments';
import cartApi from '../api/cart';

export default function CheckoutPage() {
    const location = useLocation();
    const navigate = useNavigate();

    const [cartItems, setCartItems] = useState([]);
    const [subtotal, setSubtotal] = useState(0);

    const [isOrdering, setIsOrdering] = useState(false);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);

    // 주소 입력 상태
    const [formData, setFormData] = useState({
        lastName: '',       // 姓
        firstName: '',      // 名
        lastNameKana: '',
        firstNameKana: '',
        postalCode: '',
        state: '',          // 都道府県
        city: '',           // 市区町村
        street: '',         // 番地, ビル, 部屋番号
        phone: '',
        email: '',
        paymentMethod: 'credit_card',
        deliveryDate: '',
        deliveryTime: ''
    });

    /**
     * 주소 도도부현 자동 생성
     */
    const JAPANESE_STATE = [
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

    /**
     * 배송일 생성
     */
    const [deliveryDates, setDeliveryDates] = useState([]);

    // 배송 날짜 자동 생성
    const DELIVERY_TIME_SLOTS = [
        { value: 'morning', label: '午前中 (8-12時)' },
        { value: 'afternoon', label: '14-16時' },
        { value: 'evening', label: '16-18時' },
        { value: 'night', label: '18-21時' }
    ];

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

    /**
     * 장바구니 불러오기
     */
    // 1) location.state 에서 cartItems와 subtotal을 먼저 시도해서 받아옴
    // 2) 없으면 API 호출로 장바구니 데이터 가져오기
    useEffect(() => {
        if (location.state?.cartItems && location.state?.subtotal !== undefined) {
            setCartItems(location.state.cartItems);
            setSubtotal(location.state.subtotal);
        } else {
            // location.state가 없으면 API 호출로 장바구니 불러오기
            setIsLoading(true);
            cartApi.getCartItems()
                .then(data => {
                    setCartItems(data.items || []);
                    setSubtotal(data.subtotal || 0);
                })
                .catch(err => {
                    console.error('장바구니 불러오기 실패:', err);
                    setCartItems([]);
                    setSubtotal(0);
                })
                .finally(() => {
                    setIsLoading(false);
                });
        }
    }, [location.state]);

    // 장바구니가 비어있으면 메시지 띄우기
    if (!isLoading && cartItems.length === 0) {
        return <div>カートの情報がありません。</div>;
    }

    // 총 주문 금액 계산
    const calculatedSubtotal = subtotal || cartItems.reduce((sum, item) => sum + (item.priceAtAddition * item.quantity), 0);
    const shippingFee = 600;
    const tax = Math.floor(calculatedSubtotal * 0.1);
    const totalAmount = calculatedSubtotal + shippingFee + tax;

    /**
     * Handlers
     */
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handlePostalCodeLookup = async () => {
        const postalCode = formData.postalCode.replace('-', '');
        if (postalCode.length !== 7) return;

        setIsLoading(true);

        try {
            const response = await fetch(`https://zipcloud.ibsnet.co.jp/api/search?zipcode=${postalCode}`);
            const data = await response.json();

            if (data.status === 200 && data.results) {
                const result = data.results[0];
                setFormData(prev => ({
                    ...prev,
                    prefecture: result.address1,
                    city: result.address2,
                    address: result.address3
                }));
            } else {
                alert('住所が見つかりませんでした');
            }
        } catch (error) {
            alert('住所の取得に失敗しました');
        } finally {
            setIsLoading(false);
        }
    };

    // 랜덤 트랜잭션 ID 생성
    function generateTransactionId() {
        return 'txn_' + Math.random().toString(36).substr(2, 9);
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsOrdering(true);
        setError(null);

        try {
            // DTO에 맞게 주문 생성
            const orderPayload = {
                customer: {
                    lastName: formData.lastName,
                    firstName: formData.firstName,
                    lastNameKana: formData.lastNameKana,
                    firstNameKana: formData.firstNameKana,
                    phone: formData.phone,
                    email: formData.email,
                },
                delivery: {
                    date: formData.deliveryDate,
                    time: formData.deliveryTime,
                    shippingAddress: {
                        // addresstype 생략
                        postalCode: formData.postalCode,
                        state: formData.state,   // 도도부현
                        city: formData.city,     // 시구군
                        street: formData.street, // 상세주소 (번지+빌딩명+방번호 등)
                        country: "JAPAN",
                        isDefault: false,
                    },
                    billingAddress: null, // 필요시 추가 가능
                },
                paymentMethod: formData.paymentMethod,
                cartItems: cartItems,
            };

            const orderData = await orderApi.createOrder(orderPayload);
            const orderId = orderData.id;

            const paymentRequest = {
                orderId,
                paymentMethod: formData.paymentMethod,
                amount: total,
                transactionId: generateTransactionId(),
            };

            await paymentsApi.createPayment(paymentRequest);

            alert('注文が確定しました！');
            navigate('/order-success', { state: { orderId } });

        } catch (error) {
            console.error('決済 or 注文生成失敗:', error);
            setError('決済処理に失敗しました。もう一度お試しください。');
        } finally {
            setIsOrdering(false);
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
                            <label className="block text-sm font-medium text-gray-700 mb-1">お名前（漢字）</label>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    name="lastName"
                                    placeholder="姓"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                    required
                                    pattern="[\u4E00-\u9FFF]+"
                                    title="漢字で入力してください。"
                                    className="w-1/2 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                                <input
                                    type="text"
                                    name="firstName"
                                    placeholder="名"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                    required
                                    pattern="[\u4E00-\u9FFF]+"
                                    title="漢字で入力してください。"
                                    className="w-1/2 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                            </div>
                        </div>

                        <div className="mb-4">
                            <label className="block text-sm font-medium text-gray-700 mb-1">お名前（カタカナ）</label>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    name="lastNameKana"
                                    placeholder="セイ"
                                    value={formData.lastNameKana}
                                    onChange={handleChange}
                                    required
                                    pattern="[\u30A1-\u30FC]+"
                                    title="全角カタカナで入力してください"
                                    className="w-1/2 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                                <input
                                    type="text"
                                    name="firstNameKana"
                                    placeholder="メイ"
                                    value={formData.firstNameKana}
                                    onChange={handleChange}
                                    required
                                    pattern="[\u30A1-\u30FC]+"
                                    title="全角カタカナで入力してください"
                                    className="w-1/2 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                            </div>
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
                                    pattern="^\d{3}-?\d{4}$"
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
                            <label htmlFor="state" className="block text-sm font-medium text-gray-700 mb-1">都道府県</label>
                            <select
                                id="state"
                                name="state"
                                value={formData.state}
                                onChange={handleChange}
                                required
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="">選択してください</option>
                                {JAPANESE_STATE.map(pref => (
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
                            <label htmlFor="street" className="block text-sm font-medium text-gray-700 mb-1">番地（ビル名, 部屋番号）</label>
                            <input
                                type="text"
                                id="street"
                                name="street"
                                value={formData.street}
                                onChange={handleChange}
                                placeholder="例：1-2-3 ABCビル 101号"
                                required
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
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="">指定なし</option>
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
                                                        src={item.productImageUrl}
                                                        alt={item.productName}
                                                        className="w-full h-full object-cover"
                                                    />
                                                </div>
                                            </div>
                                            <div className="flex-1">
                                                <h3 className="font-medium text-gray-800">{item.productName}</h3>
                                                <p className="text-sm text-gray-600">
                                                    ¥{Number(item.priceAtAddition ?? 0).toLocaleString()} × {item.quantity}
                                                </p>
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
                                        <span className="font-bold text-gray-900">¥{totalAmount.toLocaleString()}</span>
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