import React from 'react';

/**
 * 仮想決済方法選択コンポーネント(決済手段別残高管理)
 * @param {object} props
 * @param {object} props.formData - フォームデータ
 * @param {function} props.handleChange - フォームフィールド変更ハンドラー
 * @param {object} props.userBalances - 各決済手段の現在の残高 {point: number, paypay: number, virtualCreditCard: number}
 * @param {number} props.totalAmount - 注文合計金額
 * @param {function} props.handleChargePoints - ポイントチャージハンドラー
 * @param {object} props.cardData - クレジットカード情報
 * @param {function} props.handleCardChange - クレジットカード情報の変更を処理する関数
 * @param {boolean} props.isCharging - チャージ処理中かどうか
 */
export default function CheckoutPaymentMethods({
    formData,
    handleChange,
    userBalances = { point: 0, paypay: 0, virtualCreditCard: 0 },
    totalAmount = 0,
    handleChargePoints,
    cardData,
    handleCardChange,
    isCharging,
}) {

    // 現在選択されている支払い手段の残高を取得
    const selectedMethod = formData.paymentMethod;
    // userBalancesのキー名とformDataのキー名が異なるため調整
    const balanceKey = selectedMethod === 'virtual_credit_card' ? 'virtualCreditCard' : selectedMethod;
    const currentBalance = userBalances[balanceKey] || 0;

    // 現在選択された方法で決済が可能か確認
    const canPayWithSelectedMethod = currentBalance >= totalAmount;

    // 各決済手段が選択されているか確認
    const isPointSelected = selectedMethod === 'point';
    const isPayPaySelected = selectedMethod === 'paypay';
    const isVirtualCreditCardSelected = selectedMethod === 'virtual_credit_card';

    // 支払い方法のオプション
    const paymentOptions = [
        { value: 'point', label: 'ポイントで支払い', balance: userBalances.point, balanceLabel: 'ポイント' },
        { value: 'paypay', label: 'PayPayで支払い', balance: userBalances.paypay, balanceLabel: '円' },
        { value: 'virtual_credit_card', label: 'クレジットカード', balance: userBalances.virtualCreditCard, balanceLabel: '円' },
    ];

    return (
        <section className="bg-white p-6 rounded-lg shadow-md mb-6">
            <h2 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">お支払い方法</h2>

            <div className="space-y-3 mb-6">
                {paymentOptions.map(option => (
                    <label
                        key={option.value}
                        // 選択状態とUIを同期させるためのスタイル
                        className={`flex items-center space-x-2 p-3 rounded-md transition-all cursor-pointer ${
                            selectedMethod === option.value ? 'bg-blue-50 border border-blue-200' : 'bg-white hover:bg-gray-50'
                        }`}
                    >
                        <input
                            type="radio"
                            name="paymentMethod"
                            value={option.value}
                            checked={selectedMethod === option.value}
                            onChange={handleChange}
                            className="h-4 w-4 text-blue-600 focus:ring-blue-500"
                            // 残高に関わらず選択できるように、disabled属性を削除
                        />
                        <span className="text-gray-700">
                            {option.label}
                        </span>
                        <span className="ml-auto text-sm text-gray-500">
                            残高: {option.balance.toLocaleString()} {option.balanceLabel}
                        </span>
                    </label>
                ))}
            </div>

            {/* 仮想クレジットカード選択時のフォーム画面 */}
            {isVirtualCreditCardSelected && (
                <div className="space-y-4 bg-gray-50 p-4 rounded-md">
                    <div>
                        <label htmlFor="cardNumber" className="block text-sm font-medium text-gray-700 mb-1">カード番号</label>
                        <input
                            type="text"
                            id="cardNumber"
                            name="cardNumber"
                            value={cardData.cardNumber}
                            onChange={handleCardChange}
                            placeholder="1234 5678 9012 3456"
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label htmlFor="cardExpiry" className="block text-sm font-medium text-gray-700 mb-1">有効期限</label>
                            <input
                                type="text"
                                id="cardExpiry"
                                name="cardExpiry"
                                value={cardData.cardExpiry}
                                onChange={handleCardChange}
                                placeholder="MM/YY"
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div>
                            <label htmlFor="cardCvv" className="block text-sm font-medium text-gray-700 mb-1">セキュリティコード</label>
                            <input
                                type="text"
                                id="cardCvv"
                                name="cardCvv"
                                value={cardData.cardCvv}
                                onChange={handleCardChange}
                                placeholder="CVC"
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                    </div>
                    <p className="text-sm text-gray-600 mt-2">
                        ※ このカード情報は保存されず、決済のために使用されます。
                    </p>
                </div>
            )}

            {/* 選択された決済手段の決済情報の表示 */}
            {selectedMethod && (
                <div className="bg-gray-50 p-4 rounded-md mt-4">
                    <div className="flex justify-between items-center text-sm font-medium text-gray-700">
                        <span>現在の残高:</span>
                        <span className="text-blue-600 font-bold">
                            {currentBalance.toLocaleString()} {isPointSelected ? 'ポイント' : '円'}
                        </span>
                    </div>
                    <div className="flex justify-between items-center text-sm font-medium text-gray-700 mt-2">
                        <span>お支払い金額:</span>
                        <span className="text-red-600 font-bold">
                            - {totalAmount.toLocaleString()} {isPointSelected ? 'ポイント' : '円'}
                        </span>
                    </div>
                    {canPayWithSelectedMethod && (
                        <div className="mt-4 pt-4 border-t border-gray-200 flex justify-between items-center text-base font-bold text-gray-800">
                            <span>お支払い後の残高:</span>
                            <span>
                                {(currentBalance - totalAmount).toLocaleString()} {isPointSelected ? 'ポイント' : '円'}
                            </span>
                        </div>
                    )}
                </div>
            )}

            {/* 選択された決済手段で決済するポイントが足りない場合、チャージボタンを表示 */}
            {!canPayWithSelectedMethod && (
                <div className="bg-red-50 p-4 rounded-md mt-6">
                    <p className="text-red-700 text-sm mb-3">
                        決済に必要な {isPointSelected ? 'ポイント' : '金額'}が {totalAmount.toLocaleString()} {isPointSelected ? 'ポイント' : '円'}に対し、<br />
                        現在の残高は {currentBalance.toLocaleString()} {isPointSelected ? 'ポイント' : '円'}です。
                    </p>
                    <button
                        type="button"
                        onClick={() => handleChargePoints(balanceKey)}
                        className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-md transition-colors duration-200"
                        disabled={isCharging}
                    >
                        {isCharging ? 'チャージ中...' : `${isPointSelected ? 'ポイント' : '残高'}をチャージする`}
                    </button>
                </div>
            )}
        </section>
    );
}
