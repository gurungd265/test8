import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCreditCard, faMobileAlt } from '@fortawesome/free-solid-svg-icons';

import * as paymentRegistrationApi from '../api/paymentRegistration';
import PaypayRegistrationForm from '../components/payment/PaypayRegistrationForm';
import CreditCardRegistrationForm from '../components/payment/CreditCardRegistrationForm';

export default function PaymentRegistrationPage() {
    const { user, isLoggedIn } = useAuth();
    const navigate = useNavigate();
    const [selectedMethod, setSelectedMethod] = useState('paypay');
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState(null);
    const [paypayId, setPaypayId] = useState('');

    const handlePaypaySubmit = async () => {
        setIsLoading(true);
        setMessage(null);
        try {
            if (!user || !user.email) {
                throw new Error('ユーザー情報がありません。');
            }
            await paymentRegistrationApi.registerPayPay(user.email, paypayId);
            setMessage({ type: 'success', text: `PayPayアカウント (${paypayId}) の登録が完了しました。` });
            setTimeout(() => navigate('/mypoint'), 2000);
        } catch (err) {
            console.error("PayPay登録に失敗しました:", err);
            const errorMsg = err.response?.data || err.message || '登録中にエラーが発生しました。';
            setMessage({ type: 'error', text: errorMsg });
        } finally {
            setIsLoading(false);
        }
    };

    const handleCreditCardSubmit = async (cardInfo) => {
        setIsLoading(true);
        setMessage(null);
        try {
            if (!user || !user.email) {
                throw new Error('ユーザー情報がありません。');
            }
            const fullCardInfo = {
                userId: user.email,
                ...cardInfo,
            };
            await paymentRegistrationApi.registerCard(fullCardInfo);
            setMessage({ type: 'success', text: 'クレジットカードの登録が完了しました。' });
            setTimeout(() => navigate('/mypoint'), 2000);
        } catch (err) {
            console.error("クレジットカード登録に失敗しました:", err);
            const errorMsg = err.response?.data || err.message || '登録中にエラーが発生しました。';
            setMessage({ type: 'error', text: errorMsg });
        } finally {
            setIsLoading(false);
        }
    };

    if (!isLoggedIn) {
        return (
            <div className="container mx-auto px-4 py-8 max-w-4xl text-center text-gray-700">
                <p>このページにアクセスするにはログインが必要です。</p>
                <button onClick={() => navigate('/login')} className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
                    ログインページへ
                </button>
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8 max-w-4xl">
            <h1 className="text-3xl font-bold mb-8 text-gray-800 border-b pb-4">決済手段登録</h1>

            <div className="flex justify-center mb-8 gap-4">
                <button
                    onClick={() => setSelectedMethod('paypay')}
                    className={`px-6 py-3 rounded-md font-semibold transition-colors flex items-center gap-2 ${
                        selectedMethod === 'paypay' ? 'bg-indigo-600 text-white shadow-lg' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                    }`}
                >
                    <FontAwesomeIcon icon={faMobileAlt} />
                    <span>PayPay登録</span>
                </button>
                <button
                    onClick={() => setSelectedMethod('creditCard')}
                    className={`px-6 py-3 rounded-md font-semibold transition-colors flex items-center gap-2 ${
                        selectedMethod === 'creditCard' ? 'bg-indigo-600 text-white shadow-lg' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                    }`}
                >
                    <FontAwesomeIcon icon={faCreditCard} />
                    <span>クレジットカード登録</span>
                </button>
            </div>

            <div className="bg-white p-8 rounded-lg shadow-lg border border-gray-200">
                {selectedMethod === 'paypay' ? (
                    <div className="space-y-4">
                        <label className="block text-gray-700">PayPay ID</label>
                        <input
                            type="text"
                            value={paypayId}
                            onChange={(e) => setPaypayId(e.target.value)}
                            className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                            placeholder="あなたのPayPay IDを入力してください"
                        />
                        <button
                            onClick={handlePaypaySubmit}
                            disabled={isLoading || !paypayId}
                            className="w-full px-6 py-3 bg-indigo-600 text-white font-bold rounded-md hover:bg-indigo-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {isLoading ? '登録中...' : 'PayPayを登録する'}
                        </button>
                        {message && (
                            <div className={`mt-4 px-4 py-3 rounded-md text-sm ${message.type === 'success' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                                {message.text}
                            </div>
                        )}
                    </div>
                ) : (
                    <CreditCardRegistrationForm
                        onSubmit={handleCreditCardSubmit}
                        isLoading={isLoading}
                        message={message}
                    />
                )}
            </div>

            <div className="mt-8 text-center">
                <button
                    onClick={() => navigate('/my-points')}
                    className="px-6 py-3 bg-gray-500 text-white rounded-md hover:bg-gray-600 transition-colors"
                >
                    ポイントページに戻る
                </button>
            </div>
        </div>
    );
}