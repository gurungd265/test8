import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { CheckCircle, ShoppingBag, Home, ShoppingCart, Truck, CreditCard, User } from 'lucide-react';

export default function OrderSuccess() {
    const { state } = useLocation();
    const navigate = useNavigate();
    const { user, isLoggedIn } = useAuth();

    const orderId = state?.orderId;
    const orderDetails = state?.orderDetails;

    if (!orderId || !orderDetails) {
        return (
            <div className="container mx-auto px-4 py-24 max-w-4xl text-center">
                <div className="bg-white rounded-xl shadow-2xl p-10">
                    <h1 className="text-3xl font-bold text-gray-800 mb-4">エラーが発生しました</h1>
                    <p className="text-lg text-gray-600 mb-6">
                        注文情報を見つけることができません。
                    </p>
                    <button
                        onClick={() => navigate('/')}
                        className="flex items-center justify-center gap-2 px-8 py-4 bg-blue-600 text-white rounded-lg shadow-lg hover:bg-blue-700 transition-colors transform hover:scale-105"
                    >
                        <Home size={20} />
                        ホームに戻る
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-12 max-w-5xl">
            <div className="bg-white rounded-xl shadow-2xl p-10 text-center">

                {/* 注文完了の成功アニメーションとメッセージ */}
                <div className="flex justify-center mb-6 animate-fade-in-up">
                    <CheckCircle className="w-20 h-20 text-green-500 animate-check-icon" strokeWidth={1.2} />
                </div>

                <h1 className="text-3xl font-extrabold text-gray-800 mb-4 animate-fade-in">
                    {isLoggedIn && user ? `${user.lastName}${user.firstName}様、ご注文ありがとうございます！` : 'ご注文ありがとうございます！'}
                </h1>

                <p className="text-lg text-gray-600 mb-8 animate-fade-in-delay-100">
                    ご注文が正常に処理されました。注文確認メールを送信しました。
                </p>

                {/* 注文概要カード */}
                <div className="bg-gray-50 rounded-xl p-6 mb-8 text-left shadow-inner">
                    <div className="flex items-center gap-3 border-b border-gray-200 pb-4 mb-4">
                        <ShoppingBag className="text-gray-700 w-6 h-6" />
                        <span className="text-lg font-bold text-gray-800">注文番号: {orderId}</span>
                    </div>

                    {/* Ordered product list */}
                    <div className="mb-4">
                        <h3 className="text-lm font-bold text-gray-700 mb-4">ご注文商品</h3>
                        <ul className="space-y-4">
                            {orderDetails.cartItems.map(item => (
                                <li key={item.id} className="flex gap-4 items-start border-b border-gray-200 pb-4 last:border-b-0">
                                    {/* Image */}
                                    <img
                                        src={item.productImageUrl}
                                        alt={item.productName}
                                        className="w-16 h-16 sm:w-20 sm:h-20 object-cover rounded-md flex-shrink-0"
                                    />

                                    {/* Product name and options */}
                                    <div className="flex flex-col flex-grow">
                                        <span className="text-sm font-semibold text-gray-800">{item.productName}</span>
                                        {/* Options */}
                                        {item.options && item.options.length > 0 && (
                                            <ul className="mt-2 text-xs text-gray-600 space-y-1">
                                                {item.options.map(opt => (
                                                    <li key={opt.id}>
                                                        <span className="font-medium">{opt.optionName}:</span> {opt.optionValue}
                                                    </li>
                                                ))}
                                            </ul>
                                        )}
                                    </div>

                                    {/* Price and quantity */}
                                    <div className="text-right text-sm text-gray-800 font-medium whitespace-nowrap">
                                        ¥{(item.priceAtAddition * item.quantity).toLocaleString()} <br />
                                        <span className="text-xs text-gray-500">({item.quantity}点)</span>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    </div>

                    {/* お客様、決済、配送情報 */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-6 pt-4 border-t border-gray-200">
                        {isLoggedIn && user && (
                            <div>
                                <h3 className="text-lm font-bold text-gray-700 mb-2 flex items-center gap-2"><User size={20} className="text-blue-500"/>お客様情報</h3>
                                <p className="text-sm text-gray-600">{user.lastName} {user.firstName} 様</p>
                                <p className="text-sm text-gray-600">{user.email}</p>
                            </div>
                        )}
                        <div>
                            <h3 className="text-lm font-bold text-gray-700 mb-2 flex items-center gap-2"><CreditCard size={20} className="text-blue-500"/>お支払い情報</h3>
                            <p className="text-sm text-gray-600">お支払い方法: {orderDetails.paymentMethod === 'paypay' ? 'PayPay' : 'ポイント'}</p>
                            <p className="text-sm font-bold text-gray-800 mt-2">合計金額: ¥{orderDetails.totalAmount.toLocaleString()}</p>
                        </div>
                        <div>
                            <h3 className="text-lm font-bold text-gray-700 mb-2 flex items-center gap-2"><Truck size={20} className="text-blue-500"/>配送先情報</h3>
                            <p className="text-sm text-gray-600">〒{orderDetails.address.postalCode}</p>
                            <p className="text-sm text-gray-600">{orderDetails.address.state} {orderDetails.address.city} {orderDetails.address.street}</p>
                            <p className="text-sm text-gray-600 mt-1">お届け予定日: {orderDetails.deliveryDate}</p>
                        </div>
                    </div>
                </div>

                {/* button */}
                <div className="flex flex-col sm:flex-row justify-center gap-4 animate-fade-in-delay-200">
                    <button
                        onClick={() => navigate(`/my-orders/${orderId}`)}
                        className="flex items-center justify-center gap-2 px-8 py-4 bg-green-500 text-white rounded-lg shadow-lg hover:bg-green-600 transition-colors transform hover:scale-105"
                    >
                        <ShoppingBag size={18} />
                        注文詳細を確認
                    </button>
                    <button
                        onClick={() => navigate('/')}
                        className="flex items-center justify-center gap-2 px-8 py-4 bg-gray-200 text-gray-800 rounded-lg shadow-lg hover:bg-gray-300 transition-colors transform hover:scale-105"
                    >
                    <Home size={18} />
                    ホームに戻る
                    </button>
                </div>
            </div>
        </div>
    );
}