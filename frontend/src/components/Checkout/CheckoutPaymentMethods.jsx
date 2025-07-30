import React from 'react';

export default function CheckoutPaymentMethods({ formData, handleChange }) {
    return (
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
    );
}
