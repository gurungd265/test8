import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import * as virtualPaymentsApi from '../api/virtualPayments';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCoins, faPlusCircle } from '@fortawesome/free-solid-svg-icons';

export default function MyPointPage() {
    const { user, isLoggedIn } = useAuth();
    const navigate = useNavigate();
    const [pointBalance, setPointBalance] = useState(0);
    const [chargeAmount, setChargeAmount] = useState(1000);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [chargeError, setChargeError] = useState(null);
    const [isCharging, setIsCharging] = useState(false);

    const fetchPointBalance = async () => {
        if (!isLoggedIn || !user) {
            setIsLoading(false);
            return;
        }

        try {
            const fetchedBalances = await virtualPaymentsApi.fetchBalances(user.email);
            setPointBalance(fetchedBalances.point || 0);
            setError(null);
        } catch (err) {
            console.error("ポイント残高の取得に失敗しました:", err);
            setError('ポイント残高を読み込む中にエラーが発生しました。');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchPointBalance();
    }, [isLoggedIn, user]);

    const handleChargePoints = async () => {
        setChargeError(null);
        setIsCharging(true);

        if (!user || !user.email) {
            setChargeError('ユーザー情報がありません。');
            setIsCharging(false);
            return;
        }

        if (chargeAmount <= 0) {
            setChargeError('1円以上をチャージしてください。');
            setIsCharging(false);
            return;
        }

        try {
            await virtualPaymentsApi.chargePoints(user.email, 'point', chargeAmount);
            await fetchPointBalance(); // チャージ後に残高を再取得
            setChargeAmount(1000); // フォームをリセット
            alert(`${chargeAmount}円分のポイントをチャージしました。`);
        } catch (err) {
            console.error("ポイントチャージに失敗しました:", err);
            // エラーメッセージをより詳細に表示
            if (err.response && err.response.data) {
                 setChargeError(err.response.data);
            } else {
                 setChargeError('チャージ中にエラーが発生しました。');
            }
        } finally {
            setIsCharging(false);
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
                <div className="text-center py-10">ポイント残高を読み込む中...</div>
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

            <div className="bg-white p-6 rounded-lg shadow-md border border-gray-200">
                <div className="flex items-center mb-4">
                    <FontAwesomeIcon icon={faCoins} className="text-yellow-500 text-2xl mr-3" />
                    <h2 className="text-2xl font-semibold text-gray-700">保有ポイント</h2>
                </div>
                <p className="text-5xl font-bold text-blue-600">
                    {pointBalance.toLocaleString()} <span className="text-xl font-normal text-gray-500">ポイント</span>
                </p>
            </div>

            <div className="mt-8 bg-white p-6 rounded-lg shadow-md border border-gray-200">
                <div className="flex items-center mb-4">
                    <FontAwesomeIcon icon={faPlusCircle} className="text-green-500 text-2xl mr-3" />
                    <h2 className="text-2xl font-semibold text-gray-700">ポイントチャージ</h2>
                </div>
                <p className="text-sm text-gray-500 mb-4">
                    ※ PayPay残高でポイントをチャージします。（1000ポイント単位）
                </p>

                {chargeError && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4 text-sm" role="alert">
                        <span className="block sm:inline">{chargeError}</span>
                    </div>
                )}

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
                        onClick={handleChargePoints}
                        disabled={isCharging}
                        className="w-full sm:w-auto px-6 py-3 bg-green-500 text-white font-bold rounded-md hover:bg-green-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {isCharging ? 'チャージ中...' : 'ポイントをチャージする'}
                    </button>
                </div>
            </div>

            <div className="mt-8 text-center">
                <button
                    onClick={() => navigate('/')}
                    className="px-6 py-3 bg-gray-500 text-white rounded-md hover:bg-gray-600 transition-colors"
                >
                    メインページに戻る
                </button>
            </div>
        </div>
    );
}
