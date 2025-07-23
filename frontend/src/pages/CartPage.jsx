import { useState } from "react";
import { Link } from "react-router-dom";

const initialCart = [
    {
        id: 1,
        name: 'Apple Airpods Pro 2',
        price: 29800,
        quantity: 1,
        image: 'https://m.media-amazon.com/images/I/61Ima+Z8XZL._AC_SL1500_.jpg'
    },
    {
        id: 2,
        name: 'Amazon Fire TV Stick HD',
        price: 39800,
        quantity: 1,
        image: 'https://m.media-amazon.com/images/I/61RESPL0N5L._AC_SL1500_.jpg'
    }
]

export default function CartPage() {
  const [cart, setCart] = useState(initialCart);

  const handleRemove = (id) => {
      setCart(cart.filter((item) => item.id !== id));
  };

  const handleQuantityChange = (id, delta) => {
      setCart(
          cart.map((item) =>
            item.id === id
                ? {...item, quantity: Math.max(1, item.quantity + delta) }
                : item
          )
      )
  }

  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

  return (
      <div className="max-w-6xl mx-auto p-6">
          <h1 className="text-2xl font-bold mb-4">ショッピングカート</h1>

          {cart.length === 0 ? (
              <div className="text-gray-600 text-center py-10">
                  カートは空です。
              </div>
          ) : (
              <>
              {/*  Product List */}
              <div className="space-y-4">
                  {cart.map((item) => (
                      <div
                        key={item.id}
                        className="flex items-center justify-between bg-white rounded shadow p-4"
                      >
                        <div className="flex items-center gap-4">
                            <Link to={`/product/${item.id}`}>
                                <img
                                    src={item.image}
                                    alt={item.name}
                                    className="w-20 h-20 object-cover rounded"
                                />
                            </Link>
                            <div>
                                <Link to={`/product/${item.id}`}>
                                    <h2 className="font-semibold">{item.name}</h2>
                                    <p className="text-gray-500">
                                        {item.price.toLocaleString()}￥
                                    </p>
                                </Link>
                                {/*Quantity Controls*/}
                                <div className="mt-2 flex items-center gap-2">
                                    <button
                                        onClick={() => handleQuantityChange(item.id, -1)}
                                        className="h-10 w-10 px-2 py-1 border rounded"
                                    >
                                        -
                                    </button>
                                    <span>{item.quantity}</span>
                                    <button
                                        onClick={() => handleQuantityChange(item.id, 1)}
                                        className="h-10 w-10 px-2 py-1 border rounded"
                                    >
                                        ＋
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div className="text-right">
                            <p className="font-bold">
                                {(item.price * item.quantity).toLocaleString()}￥
                            </p>
                            <button
                                onClick={() => handleRemove(item.id)}
                                className="text-red-500 text-sm hover:underline mt-2"
                            >
                                削除
                            </button>
                        </div>
                      </div>
                  ))}
              </div>

              {/*Summary*/}
              <div className="mt-8 bg-white rounded shadow p-4 text-right">
                  <h3 className="text-lg font-semibold mb-2">
                      Total: {total.toLocaleString()}￥
                  </h3>
                  <button className="bg-purple-600 text-white px-6 py-2 rounded hover:bg-purple-700">
                      チェックアウトに進む
                  </button>
              </div>
            </>
          )}
      </div>
  );
}