import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCoins, faCreditCard, faMobileAlt, faPlus, faCheckCircle } from '@fortawesome/free-solid-svg-icons';
import { useAuth } from '../../contexts/AuthContext';
import * as virtualPaymentsApi from '../../api/virtualPayments';

// 決済手段情報を取得するダミーAPI（モックアップ）
// 実際のAPIが実装されたらこの部分を置き換える必要があります。
const mockApi = {
    fetchPaymentMethods: async (userId) => {
        console.log(`Fetching payment methods for user: ${userId}`);
        return new Promise(resolve => {
            setTimeout(() => {
                // テスト用: 初期状態として、両方の決済手段が未登録であると仮定
                const isCardRegistered = false;
                const isPaypayRegistered = false;
                resolve({
                    creditCard: isCardRegistered ? { last4: '1234', brand: 'Visa' } : null,
                    paypay: isPaypayRegistered ? { id: 'user123_paypay' } : null,
                });
            }, 500);
        });
    }
};

// onLogout プロパティを受け取るように修正
export default function WalletSection({ onLogout }) {
    const { user } = useAuth();
    const [pointBalance, setPointBalance] = useState(0);
    const [paymentMethods, setPaymentMethods] = useState({ creditCard: null, paypay: null });
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            if (!user) {
                setError('ユーザー情報がありません。');
                setIsLoading(false);
                return;
            }

            try {
                // APIが404エラーを返すため、安全に処理するか、モックデータに切り替えます。
                // ここでは、APIが利用できない場合を考慮し、エラーをキャッチします。
                let balances = { point: 0 };
                try {
                    const apiBalances = await virtualPaymentsApi.fetchBalances(user.email);
                    balances = apiBalances;
                } catch (apiError) {
                    console.error("[API] 残高照会失敗: ", apiError);
                    // APIが利用できない場合、ポイント残高は0として扱います
                    balances = { point: 0 };
                }

                const methods = await mockApi.fetchPaymentMethods(user.email);

                setPointBalance(balances.point || 0);
                setPaymentMethods(methods);
                setError(null);
            } catch (err) {
                console.error('Failed to fetch wallet data:', err);
                setError('ウォレット情報の読み込みに失敗しました。');
            } finally {
                setIsLoading(false);
            }
        };

        fetchData();
    }, [user]);

    if (isLoading) {
        return (
            <div className="text-center py-10 text-gray-500">
                ウォレット情報を読み込み中...
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
                    <span className="block sm:inline">{error}</span>
                </div>
            )}

            {/* マイポイントセクション */}
            <div className="bg-white p-6 rounded-lg shadow-md border border-gray-200">
                <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center">
                        <FontAwesomeIcon icon={faCoins} className="text-yellow-500 text-2xl mr-3" />
                        <h2 className="text-2xl font-semibold text-gray-700">保有ポイント</h2>
                    </div>
                    <Link
                        to="/my-points"
                        className="px-4 py-2 bg-green-500 text-white font-medium rounded-md hover:bg-green-600 transition-colors"
                    >
                        ポイントチャージ
                    </Link>
                </div>
                <p className="text-4xl font-bold text-blue-600">
                    {pointBalance.toLocaleString()} <span className="text-lg font-normal text-gray-500">ポイント</span>
                </p>
            </div>

            {/* 決済手段セクション */}
            <div className="bg-white p-6 rounded-lg shadow-md border border-gray-200">
                <h2 className="text-2xl font-semibold text-gray-700 mb-4">決済手段</h2>
                <p className="text-sm text-gray-500 mb-4">
                    ※ ポイントはPayPayでのみチャージ可能です。
                </p>
                <div className="space-y-4">
                    {/* PayPay 情報 */}
                    <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg border">
                        <div className="flex items-center">
                            <FontAwesomeIcon icon={faMobileAlt} className="text-indigo-600 text-xl mr-4" />
                            <div className="flex flex-col">
                                <span className="text-lg font-medium text-gray-800">PayPay</span>
                                {paymentMethods.paypay ? (
                                    <span className="text-sm text-gray-500">登録済み</span>
                                ) : (
                                    <span className="text-sm text-gray-500">未登録</span>
                                )}
                            </div>
                        </div>
                        <Link to="/payment-registration" className="text-indigo-600 hover:text-indigo-800 transition-colors">
                            {paymentMethods.paypay ? (
                                <FontAwesomeIcon icon={faCheckCircle} className="text-green-500 text-2xl" />
                            ) : (
                                <FontAwesomeIcon icon={faPlus} className="text-gray-400 text-xl" />
                            )}
                        </Link>
                    </div>

                    {/* クレジットカード情報 */}
                    <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg border">
                        <div className="flex items-center">
                            <FontAwesomeIcon icon={faCreditCard} className="text-indigo-600 text-xl mr-4" />
                            <div className="flex flex-col">
                                <span className="text-lg font-medium text-gray-800">クレジットカード</span>
                                {paymentMethods.creditCard ? (
                                    <span className="text-sm text-gray-500">**** **** **** {paymentMethods.creditCard.last4}</span>
                                ) : (
                                    <span className="text-sm text-gray-500">未登録</span>
                                )}
                            </div>
                        </div>
                        <Link to="/payment-registration" className="text-indigo-600 hover:text-indigo-800 transition-colors">
                            {paymentMethods.creditCard ? (
                                <FontAwesomeIcon icon={faCheckCircle} className="text-green-500 text-2xl" />
                            ) : (
                                <FontAwesomeIcon icon={faPlus} className="text-gray-400 text-xl" />
                            )}
                        </Link>
                    </div>
                </div>
            </div>

            {/* ログアウトボタン（onLogout プロパティを使用するように修正） */}
            <div className="flex justify-end mt-6">
                <button
                    onClick={onLogout}
                    className="px-4 py-2 border border-red-500 text-red-500 rounded hover:bg-red-100 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-opacity-50"
                >
                    ログアウト
                </button>
            </div>
        </div>
    );
}
