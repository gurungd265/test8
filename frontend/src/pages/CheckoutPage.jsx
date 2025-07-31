import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import orderApi from '../api/order';
import paymentsApi from '../api/payments';

import useCheckoutData from '../hooks/useCheckoutData';
import useDeliveryOptions from '../hooks/useDeliveryOptions'; // 新しい配送オプションフック
import { generateTransactionId } from '../utils/paymentUtils'; // 新しいユーティリティ関数

import CheckoutCustomerInfo from '../components/Checkout/CheckoutCustomerInfo';
import CheckoutAddressSelection from '../components/Checkout/CheckoutAddressSelection';
import CheckoutDeliveryOptions from '../components/Checkout/CheckoutDeliveryOptions';
import CheckoutPaymentMethods from '../components/Checkout/CheckoutPaymentMethods';
import CheckoutOrderSummary from '../components/Checkout/CheckoutOrderSummary';

export default function CheckoutPage() {
    const navigate = useNavigate();
    const [isOrdering, setIsOrdering] = useState(false);
    const [submitError, setSubmitError] = useState(null);
    const [currentStep, setCurrentStep] = useState(1);
    const [formData, setFormData] = useState({
        lastName: '',
        firstName: '',
        lastNameKana: '',
        firstNameKana: '',
        postalCode: '',
        state: '',
        city: '',
        street: '',
        phone: '',
        email: '',
        paymentMethod: 'credit_card',
        deliveryDate: '',
        deliveryTime: ''
    });

    // ⭐ useCheckoutData フックを使用してデータと関連ステートを取得
    const {
        cartItems,
        subtotal,
        availableAddresses,
        selectedAddressId,
        setSelectedAddressId,
        isLoading, // 初期データローディング状態
        error: fetchError, // useCheckoutDataフックで発生したエラー（データロード失敗時）
        addressStatusChecked,
        fetchInitialCheckoutData, // データを再度取得する関数
    } = useCheckoutData();

    // useDeliveryOptions フックを使用して配送オプションを取得
    const { deliveryDates, DELIVERY_TIME_SLOTS } = useDeliveryOptions();

    useEffect(() => {
        fetchInitialCheckoutData(setFormData);
    }, [fetchInitialCheckoutData]); // fetchInitialCheckoutData自体がuseCallbackなので依存配列に含める

    // 画面に表示する最終的なエラー（データロードエラーまたはフォーム送信エラー）
    const displayError = fetchError || submitError;

    // addressStatusCheckedがfalseの場合、まだ住所の有無を確認中なのでローディング画面を維持
    if (isLoading || !addressStatusChecked) {
        return (
            <div className="container mx-auto px-4 py-8 max-w-6xl text-center text-gray-700">
                <div className="text-center py-10">データを読み込み中...</div>
            </div>
        );
    }

    // カートが空の場合、メッセージを表示（データロード完了後）
    if (!isLoading && cartItems.length === 0) {
        return (
            <div className="container mx-auto px-4 py-8 max-w-6xl text-center text-gray-700">
                <p>カートに商品がありません。</p>
                <button
                    onClick={() => navigate('/')}
                    className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                >
                    商品を探しに行く
                </button>
            </div>
        );
    }

    // 合計注文金額の計算
    const calculatedSubtotal = subtotal || cartItems.reduce((sum, item) => sum + (item.priceAtAddition * item.quantity), 0);
    const shippingFee = 600;
    const tax = Math.floor(calculatedSubtotal * 0.1);
    const totalAmount = calculatedSubtotal + shippingFee + tax;

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // 住所選択変更ハンドラ
    const handleAddressSelect = (e) => {
        const selectedId = e.target.value;
        setSelectedAddressId(selectedId); // useCheckoutDataフックが提供するステート更新関数
        const selectedAddr = availableAddresses.find(addr => String(addr.id) === selectedId);
        if (selectedAddr) {
            setFormData(prev => ({
                ...prev,
                postalCode: selectedAddr.postalCode || '',
                state: selectedAddr.state || '',
                city: selectedAddr.city || '',
                street: selectedAddr.street || '',
            }));
        }
    };

    // 注文送信ハンドラ
    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsOrdering(true);
        setSubmitError(null); // 送信エラーを初期化

        // ステップ3最終提出時のバリデーション
        const requiredFields = ['lastName', 'firstName', 'postalCode', 'state', 'city', 'street', 'phone', 'email', 'deliveryDate', 'deliveryTime', 'paymentMethod'];
        const missingFields = requiredFields.filter(field => !formData[field]);

        if (missingFields.length > 0) {
            setSubmitError('必須項目をすべて入力してください。');
            setIsOrdering(false);
            return;
        }

        try {
            const orderPayload = {
                customer: {
                    lastName: formData.lastName,
                    firstName: formData.firstName,
                    lastNameKana: formData.lastNameKana || '',
                    firstNameKana: formData.firstNameKana || '',
                    phone: formData.phone,
                    email: formData.email,
                },
                delivery: {
                    date: formData.deliveryDate,
                    time: formData.deliveryTime,
                    shippingAddress: {
                        postalCode: formData.postalCode,
                        state: formData.state,
                        city: formData.city,
                        street: formData.street,
                        country: "JAPAN", // 日本固定
                        isDefault: false, // 注文時にはデフォルト住所を設定しない
                    },
                    billingAddress: null, // 請求先住所は現在なし
                },
                paymentMethod: formData.paymentMethod,
                cartItems: cartItems.map(item => ({
                    productId: item.productId,
                    quantity: item.quantity,
                    priceAtAddition: item.priceAtAddition
                })),
            };

            const orderData = await orderApi.createOrder(orderPayload);
            const orderId = orderData.id;

            const paymentRequest = {
                orderId,
                paymentMethod: formData.paymentMethod,
                amount: totalAmount,
                transactionId: generateTransactionId(), // ユーティリティ関数を使用
            };

            await paymentsApi.createPayment(paymentRequest);

            alert('注文が確定しました！');
            navigate('/order-success', { state: { orderId } }); // 成功ページへ移動

        } catch (err) {
            console.error('決済または注文生成失敗:', err);
            setSubmitError('決済処理に失敗しました。もう一度お試しください。');
        } finally {
            setIsOrdering(false);
        }
    };

    // ステップ移動ハンドラ
    const handleNextStep = () => {
        // ステップ1からステップ2への移動前のバリデーション
        if (currentStep === 1) {
            const customerFields = ['lastName', 'firstName', 'phone', 'email'];
            const addressFields = ['postalCode', 'state', 'city', 'street'];

            const missingCustomerFields = customerFields.filter(field => !formData[field]);
            const missingAddressFields = addressFields.filter(field => !formData[field]);

            if (missingCustomerFields.length > 0 || missingAddressFields.length > 0) {
                setSubmitError('お客様情報と配送先情報をすべて入力してください。');
                return;
            }
        }
        // ステップ2からステップ3への移動前のバリデーション
        else if (currentStep === 2) {
            if (!formData.paymentMethod || !formData.deliveryDate || !formData.deliveryTime) {
                 setSubmitError('配送オプションと支払い方法をすべて選択してください。');
                 return;
            }
        }

        setSubmitError(null); // 次のステップに進む前にエラーを初期化
        setCurrentStep(prev => prev + 1);
    };

    const handlePrevStep = () => {
        setCurrentStep(prev => prev - 1);
        setSubmitError(null); // 前のステップに戻る時にエラーを初期化
    };

    return (
        <div className="container mx-auto px-4 py-8 max-w-6xl">
            <h1 className="text-2xl font-bold mb-8 text-gray-800 border-b pb-4">注文内容の確認</h1>

            <div className="flex flex-col lg:flex-row gap-8">
                <div className="lg:w-2/3">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-xl font-semibold text-gray-700">
                            ステップ {currentStep} / 3: {
                                currentStep === 1 ? 'お客様情報と配送先' :
                                currentStep === 2 ? '配送オプションと支払い方法' :
                                'ご注文内容の最終確認'
                            }
                        </h2>
                        <div className="flex gap-2">
                            {currentStep > 1 && (
                                <button
                                    type="button"
                                    onClick={handlePrevStep}
                                    className="px-4 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400"
                                >
                                    戻る
                                </button>
                            )}
                            {currentStep < 3 && (
                                <button
                                    type="button"
                                    onClick={handleNextStep}
                                    className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                                >
                                    次へ
                                </button>
                            )}
                        </div>
                    </div>

                    {displayError && (
                        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                            <span className="block sm:inline">{displayError}</span>
                        </div>
                    )}

                    {currentStep === 1 && (
                        <>
                            <CheckoutCustomerInfo formData={formData} handleChange={handleChange} />
                            <CheckoutAddressSelection
                                availableAddresses={availableAddresses}
                                selectedAddressId={selectedAddressId}
                                handleAddressSelect={handleAddressSelect}
                                formData={formData} // 選択された住所の情報がここに反映されることを示す
                            />
                        </>
                    )}

                    {currentStep === 2 && (
                        <>
                            <CheckoutDeliveryOptions
                                formData={formData}
                                handleChange={handleChange}
                                deliveryDates={deliveryDates} // useDeliveryOptionsから取得
                                DELIVERY_TIME_SLOTS={DELIVERY_TIME_SLOTS} // useDeliveryOptionsから取得
                            />
                            <CheckoutPaymentMethods
                                formData={formData}
                                handleChange={handleChange}
                            />
                        </>
                    )}

                    {currentStep === 3 && (
                        <form onSubmit={handleSubmit}>
                            <section className="bg-white p-6 rounded-lg shadow-md mb-6">
                                <h2 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">お客様情報</h2>
                                <p className="mb-2"><span className="font-medium">お名前:</span> {formData.lastName} {formData.firstName}{formData.lastNameKana && ` (${formData.lastNameKana} ${formData.firstNameKana})`}</p>
                                <p className="mb-2"><span className="font-medium">電話番号:</span> {formData.phone}</p>
                                <p className="mb-2"><span className="font-medium">メールアドレス:</span> {formData.email}</p>
                            </section>

                            <section className="bg-white p-6 rounded-lg shadow-md mb-6">
                                <h2 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">お届け先情報</h2>
                                <p className="mb-2"><span className="font-medium">郵便番号:</span> {formData.postalCode}</p>
                                <p className="mb-2"><span className="font-medium">住所:</span> {formData.state} {formData.city} {formData.street}</p>
                            </section>

                            <section className="bg-white p-6 rounded-lg shadow-md mb-6">
                                <h2 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">配送オプション</h2>
                                <p className="mb-2"><span className="font-medium">お届け日:</span> {deliveryDates.find(d => d.value === formData.deliveryDate)?.label || '指定なし'}</p>
                                <p className="mb-2"><span className="font-medium">お届け時間帯:</span> {DELIVERY_TIME_SLOTS.find(t => t.value === formData.deliveryTime)?.label || '指定なし'}</p>
                            </section>

                            <section className="bg-white p-6 rounded-lg shadow-md mb-6">
                                <h2 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">お支払い方法</h2>
                                <p className="mb-2"><span className="font-medium">方法:</span> {
                                    formData.paymentMethod === 'credit_card' ? 'クレジットカード' :
                                    formData.paymentMethod === 'konbini' ? 'コンビニ払い' :
                                    formData.paymentMethod === 'bank_transfer' ? '銀行振込' :
                                    formData.paymentMethod === 'cod' ? '代金引換' : ''
                                }</p>
                                {formData.paymentMethod === 'credit_card' && (
                                    <p className="text-sm text-gray-600">※カード情報は表示されません。</p>
                                )}
                            </section>

                            <button
                                type="submit"
                                className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                                disabled={isOrdering || cartItems.length === 0}
                            >
                                {isOrdering ? '注文処理中...' : '注文を確定する'}
                            </button>
                        </form>
                    )}
                </div>

                {/* 注文概要 (常に表示) */}
                <div className="lg:w-1/3">
                    <CheckoutOrderSummary
                        cartItems={cartItems}
                        calculatedSubtotal={calculatedSubtotal}
                        shippingFee={shippingFee}
                        tax={tax}
                        totalAmount={totalAmount}
                    />
                </div>
            </div>
        </div>
    );
}