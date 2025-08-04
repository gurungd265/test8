import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCoins, faCreditCard, faMobileAlt } from '@fortawesome/free-solid-svg-icons';
import * as virtualPaymentsApi from '../api/virtualPayments';
import * as paymentRegistrationApi from '../api/paymentRegistration';

export default function ChargePage() {
    const { user, isLoggedIn } = useAuth();
    const navigate = useNavigate();
    const [selectedMethod, setSelectedMethod] = useState('paypay');
    const [chargeAmount, setChargeAmount] = useState(1000);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [paypayIsRegistered, setPaypayIsRegistered] = useState(null);
    const [creditCardIsRegistered, setCreditCardIsRegistered] = useState(null);

    const checkRegistrationStatus = async () => {
        if (!user || !user.email) return;

        try {
            await virtualPaymentsApi.fetchPayPayBalance(user.email);
            setPaypayIsRegistered(true);
        } catch (e) {
            setPaypayIsRegistered(false);
        }

        try {
            await paymentRegistrationApi.checkCreditCard(user.email);
            setCreditCardIsRegistered(true);
        } catch (e) {
            setCreditCardIsRegistered(false);
        }
    };

    useEffect(() => {
        checkRegistrationStatus();
    }, [user]);

    const handleCharge = async () => {
        setError(null);
        setIsLoading(true);

        if (!user || !user.email) {
            setError('ユーザー情報がありません。');
            setIsLoading(false);
            return;
        }

        if (chargeAmount <= 0) {
            setError('1円以上をチャージしてください。');
            setIsLoading(false);
            return;
        }

        try {
            if (selectedMethod === 'paypay') {
                if (paypayIsRegistered === false) {
                    alert('PayPayアカウントが登録されていません。');
                    navigate('/payment-registration');
                    return;
                }
                await virtualPaymentsApi.chargePointsWithPayPay(user.email, chargeAmount);
                alert(`${chargeAmount}円分のポイントをPayPayでチャージしました。`);
            } else if (selectedMethod === 'creditCard') {
                 if (creditCardIsRegistered === false) {
                    alert('クレジットカードが登録されていません。');
                    navigate('/payment-registration');
                    return;
                }
                await virtualPaymentsApi.chargePointsWithCreditCard(user.email, chargeAmount);
                alert(`${chargeAmount}円分のポイントをクレジットカードでチャージしました。`);
            }
            navigate('/mypoint');
        } catch (err) {
            console.error("チャージに失敗しました:", err);
            const errorMsg = err.response?.data || 'チャージ中にエラーが発生しました。';
            setError(errorMsg);
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

    if (paypayIsRegistered === null || creditCardIsRegistered === null) {
         return (
            <div className="container mx-auto px-4 py-8 max-w-4xl text-center text-gray-700">
                <div className="text-center py-10">登録状況を確認中...</div>
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8 max-w-4xl">
            <h1 className="text-3xl font-bold mb-8 text-gray-800 border-b pb-4">ポイントチャージ</h1>

            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-6" role="alert">
                    <span className="block sm:inline">{error}</span>
                </div>
            )}

            <div className="bg-white p-8 rounded-lg shadow-lg border border-gray-200">
                <div className="mb-6">
                    <h2 className="text-xl font-semibold text-gray-700 mb-4">チャージ方法を選択</h2>
                    <div className="flex justify-center gap-4">
                        <button
                            onClick={() => setSelectedMethod('paypay')}
                            className={`px-6 py-3 rounded-md font-semibold transition-colors flex items-center gap-2 ${
                                selectedMethod === 'paypay' ? 'bg-indigo-600 text-white shadow-lg' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                            }`}
                            disabled={!paypayIsRegistered}
                        >
                            <FontAwesomeIcon icon={faMobileAlt} />
                            <span>PayPay</span>
                        </button>
                        <button
                            onClick={() => setSelectedMethod('creditCard')}
                            className={`px-6 py-3 rounded-md font-semibold transition-colors flex items-center gap-2 ${
                                selectedMethod === 'creditCard' ? 'bg-indigo-600 text-white shadow-lg' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                            }`}
                            disabled={!creditCardIsRegistered}
                        >
                            <FontAwesomeIcon icon={faCreditCard} />
                            <span>クレジットカード</span>
                        </button>
                    </div>
                    <div className="text-center text-sm text-gray-500 mt-2">
                        {!paypayIsRegistered && <p>PayPayは未登録です。決済手段登録ページで登録してください。</p>}
                        {!creditCardIsRegistered && <p>クレジットカードは未登録です。決済手段登録ページで登録してください。</p>}
                    </div>
                </div>

                <div>
                    <h2 className="text-xl font-semibold text-gray-700 mb-4">チャージ金額</h2>
                    <p className="text-sm text-gray-500 mb-4">
                        ※ 1000ポイント単位でチャージできます。
                    </p>
                    <div className="flex flex-col sm:flex-row gap-4 items-center">
                        <input
                            type="number"
                            value={chargeAmount}
                            onChange={(e) => setChargeAmount(parseInt(e.target.value))}
                            className="flex-grow p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                            min="1000"
                            step="1000"
                        />
                        <button
                            onClick={handleCharge}
                            disabled={isLoading || (selectedMethod === 'paypay' && !paypayIsRegistered) || (selectedMethod === 'creditCard' && !creditCardIsRegistered)}
                            className="w-full sm:w-auto px-6 py-3 bg-green-500 text-white font-bold rounded-md hover:bg-green-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {isLoading ? 'チャージ中...' : 'チャージする'}
                        </button>
                    </div>
                </div>
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