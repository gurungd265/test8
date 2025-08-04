import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import * as virtualPaymentsApi from '../api/virtualPayments';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCoins, faPlusCircle, faMinusCircle } from '@fortawesome/free-solid-svg-icons';

export default function MyPointPage() {
    const { user, isLoggedIn } = useAuth();
    const navigate = useNavigate();
    const [pointBalance, setPointBalance] = useState(0);
    const [paypayBalance, setPaypayBalance] = useState(null); // null로 초기화하여 등록 여부 판별
    const [refundAmount, setRefundAmount] = useState(1000);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [refundError, setRefundError] = useState(null);
    const [isRefunding, setIsRefunding] = useState(false);

    const fetchBalances = async () => {
        if (!isLoggedIn || !user) {
            setIsLoading(false);
            return;
        }

        try {
            const fetchedPoint = await virtualPaymentsApi.fetchPointBalance(user.email);
            setPointBalance(fetchedPoint);

            const fetchedPayPay = await virtualPaymentsApi.fetchPayPayBalance(user.email);
            setPaypayBalance(fetchedPayPay);
            setError(null);
        } catch (err) {
            console.error("残高の取得に失敗しました:", err);
            setError('残高を読み込む中にエラーが発生しました。');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchBalances();
    }, [isLoggedIn, user]);

    const handleRefundPoints = async () => {
        setRefundError(null);
        setIsRefunding(true);

        if (!user || !user.email) {
            setRefundError('ユーザー情報がありません。');
            setIsRefunding(false);
            return;
        }

        if (refundAmount <= 0) {
            setRefundError('1円以上を返金してください。');
            setIsRefunding(false);
            return;
        }

        if (refundAmount > pointBalance) {
            setRefundError('ポイント残高が足りません。');
            setIsRefunding(false);
            return;
        }

        if (paypayBalance === null) {
            setRefundError('PayPayアカウントが登録されていません。');
            setIsRefunding(false);
            return;
        }

        try {
            await virtualPaymentsApi.refundPointsToPayPay(user.email, refundAmount);
            await fetchBalances();
            setRefundAmount(1000);
            alert(`${refundAmount}円分のポイントをPayPayに返金しました。`);
        } catch (err) {
            console.error("ポイント返金に失敗しました:", err);
            if (err.response && err.response.data) {
                setRefundError(err.response.data);
            } else {
                setRefundError('返金中にエラーが発生しました。');
            }
        } finally {
            setIsRefunding(false);
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

    if (isLoading) {
        return (
            <div className="container mx-auto px-4 py-8 max-w-4xl text-center text-gray-700">
                <div className="text-center py-10">残高を読み込む中...</div>
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8 max-w-4xl">
            <h1 className="text-3xl font-bold mb-8 text-gray-800 border-b pb-4">マイポイント管理</h1>

            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-6" role="alert">
                    <span className="block sm:inline">{error}</span>
                </div>
            )}

            <div className="bg-white p-6 rounded-lg shadow-md border border-gray-200 flex flex-col items-center mb-8">
                <div className="flex items-center mb-4">
                    <FontAwesomeIcon icon={faCoins} className="text-yellow-500 text-3xl mr-3" />
                    <h2 className="text-2xl font-semibold text-gray-700">保有ポイント</h2>
                </div>
                <p className="text-5xl font-bold text-blue-600">
                    {pointBalance.toLocaleString()} <span className="text-xl font-normal text-gray-500">ポイント</span>
                </p>
                <div className="mt-4 text-gray-600">
                    <p>PayPay残高: {paypayBalance !== null ? `${paypayBalance.toLocaleString()} 円` : '未登録'}</p>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="bg-white p-6 rounded-lg shadow-md border border-gray-200 flex flex-col items-center">
                    <div className="flex items-center mb-4">
                        <FontAwesomeIcon icon={faPlusCircle} className="text-green-500 text-2xl mr-3" />
                        <h2 className="text-2xl font-semibold text-gray-700">ポイントチャージ</h2>
                    </div>
                    <p className="text-sm text-gray-500 mb-4 text-center">
                        チャージしたい金額と決済手段を選びます。
                    </p>
                    <button
                        onClick={() => navigate('/charge')}
                        className="w-full px-6 py-3 bg-green-500 text-white font-bold rounded-md hover:bg-green-600 transition-colors"
                    >
                        チャージページへ
                    </button>
                </div>

                <div className="bg-white p-6 rounded-lg shadow-md border border-gray-200">
                    <div className="flex items-center mb-4">
                        <FontAwesomeIcon icon={faMinusCircle} className="text-red-500 text-2xl mr-3" />
                        <h2 className="text-2xl font-semibold text-gray-700">ポイント返金</h2>
                    </div>
                    <p className="text-sm text-gray-500 mb-4">
                        ※ ポイントを現金としてPayPayへ返金します。（1000ポイント単位）
                    </p>

                    {refundError && (
                        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4 text-sm" role="alert">
                            <span className="block sm:inline">{refundError}</span>
                        </div>
                    )}

                    <div className="flex flex-col sm:flex-row gap-4 items-center">
                        <input
                            type="number"
                            value={refundAmount}
                            onChange={(e) => setRefundAmount(parseInt(e.target.value))}
                            className="flex-grow p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                            min="1000"
                            step="1000"
                        />
                        <button
                            onClick={handleRefundPoints}
                            disabled={isRefunding || paypayBalance === null}
                            className="w-full sm:w-auto px-6 py-3 bg-red-500 text-white font-bold rounded-md hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {isRefunding ? '返金中...' : '返金する'}
                        </button>
                    </div>
                </div>
            </div>

            <div className="mt-8 text-center">
                <button
                    onClick={() => navigate('/payment-registration')}
                    className="px-6 py-3 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
                >
                    決済手段を登録する
                </button>
            </div>
        </div>
    );
}