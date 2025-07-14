import {Menu, User, ShoppingCart, Heart, Search, MapPin} from 'lucide-react';

export default function App() {
  return (
    <div className="bg-gray-100 min-h-screen pb-16">
      {/* Header */}
      <header className="bg-white shadow p-4 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Menu className="lg:hidden cursor pointer" />
          {/* Logo */}
          <img 
            src="https://cal.co.jp/wordpress/wp-content/themes/temp_calrenew/img/logo.svg" 
            alt="Logo" 
            className="hidden lg:block h-8"
          />
          {/* Location */}
          <button className="flex items-center gap-1 text-sm text-gray-700">
            <MapPin size="16" />
            Tokyo
          </button>
          {/* Catalog */}
          <button className="hidden lg:block text-sm font-semibold">
            Catalog
          </button>
        </div>
        <div className="flex items-center gap-4">
          {/* Search Section */}
          <div className="flex items-center border rounded overflow-hidden bg-gray-50">
            <input 
              type="search" 
              placeholder="Search..."
              className="px-3 py-1 outline-none bg-gray-50"
            />
            <button className="p-3 bg-purple-600 text-white">
              <Search size="16" />
            </button>
          </div>
          {/* Liked Prdoucts */}
          <Heart className="cursor-pointer" /> 
          {/* Shopping Cart */}
          <ShoppingCart className="hidden lg:block cursor-pointer" />
          {/* Profile */}
          <User className="hidden lg:block cursor-pointer" />
        </div>
      </header>
      
      {/* Promotions */}
      <section className="container mx-auto p-4">
        <div className="h-40 bg-purple-200 rounded-md flex items-center justify-center">
          <h2 className="text-lg font-semibold">
            Promotions Area
          </h2>
        </div>
      </section>

      {/* Products */}
      <main className="container mx-auto p-4">
        <section className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
          {/* Product */}
          <div className="bg-white rounded shadow overflow-hidden">
            {/* Product Image */}
            <img 
              src="https://m.media-amazon.com/images/I/61Ima+Z8XZL._AC_SL1500_.jpg" 
              alt="Apple AirPods Pro 2" 
              className='w-full h-56 object-cover'
            />
            <div className="p-4">
              {/* Product Name */}
              <h2 className="font-semibold text-lg truncate">
                Apple AirPods Pro 2
              </h2>
              {/* Product Description */}
              <p className="text-sm text-gray-500 truncate">
                Apple AirPods Pro 2 ワイヤレスイヤホン、Bluetooth5.3、アクティブノイズキャンセリング、外部音取り込み、パーソナライズされた空間オーディオ、原音に忠実なサウンド、H2 チップ、USB-C 充電、防塵性能と耐汗耐水性能、「探す」対応、Qi充電、ヒアリング補助機能
              </p>
              <div className="mt-2 flex items-center justify-between">
                {/* Product Price */}
                <span className='font-bold'>29,800￥</span>
                {/* Button of Adding To Cart */}
                <button className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700">
                  Add to cart
                </button>
              </div>
            </div>
          </div>

          {/* Product */}
          <div className="bg-white rounded shadow overflow-hidden">
            {/* Product Image */}
            <img 
              src="https://m.media-amazon.com/images/I/61RESPL0N5L._AC_SL1500_.jpg" 
              alt="Amazon Fire TV Stick HD" 
              className='w-full h-56 object-cover'
            />
            <div className="p-4">
              {/* Product Name */}
              <h2 className="font-semibold text-lg truncate">
                Amazon Fire TV Stick HD
              </h2>
              {/* Product Description */}
              <p className="text-sm text-gray-500 truncate">
                Amazon Fire TV Stick HD | 大画面でフルHDの楽しさを簡単に | ストリーミングメディアプレイヤー
              </p>
              <div className="mt-2 flex items-center justify-between">
                {/* Product Price */}
                <span className='font-bold'>29,800￥</span>
                {/* Button of Adding To Cart */}
                <button className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700">
                  Add to cart
                </button>
              </div>
            </div>
          </div>

          {/* Product */}
          <div className="bg-white rounded shadow overflow-hidden">
            {/* Product Image */}
            <img 
              src="https://m.media-amazon.com/images/I/61jIhPBSuXL._AC_SY395_.jpg" 
              alt="[ACE] スーツケース" 
              className='w-full h-56 object-cover'
            />
            <div className="p-4">
              {/* Product Name */}
              <h2 className="font-semibold text-lg truncate">
                [ACE] スーツケース
              </h2>
              {/* Product Description */}
              <p className="text-sm text-gray-500 truncate">
                [ACE] スーツケース mサイズ 5泊6日 6泊7日 63L/75L キャスターストッパー 容量拡張機能 サイドフック付 3.7kg キャリーケース キャリーバッグ ファームロード No.05892
              </p>
              <div className="mt-2 flex items-center justify-between">
                {/* Product Price */}
                <span className='font-bold'>29,800￥</span>
                {/* Button of Adding To Cart */}
                <button className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700">
                  Add to cart
                </button>
              </div>
            </div>
          </div>

          {/* Product */}
          <div className="bg-white rounded shadow overflow-hidden">
            {/* Product Image */}
            <img 
              src="https://m.media-amazon.com/images/I/71IIW-jiarL._AC_SY395_.jpg" 
              alt="[アディダス] スニーカー" 
              className='w-full h-56 object-cover'
            />
            <div className="p-4">
              {/* Product Name */}
              <h2 className="font-semibold text-lg truncate">
                [アディダス] スニーカー
              </h2>
              {/* Product Description */}
              <p className="text-sm text-gray-500 truncate">
                [アディダス] スニーカー グランドコート TD ライフスタイル
              </p>
              <div className="mt-2 flex items-center justify-between">
                {/* Product Price */}
                <span className='font-bold'>29,800￥</span>
                {/* Button of Adding To Cart */}
                <button className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700">
                  Add to cart
                </button>
              </div>
            </div>
          </div>

          {/* Product */}
          <div className="bg-white rounded shadow overflow-hidden">
            {/* Product Image */}
            <img 
              src="https://m.media-amazon.com/images/I/41hrGWWy7oL._AC_SX679_.jpg" 
              alt="[オリメイク] 私は佐藤ではありません" 
              className='w-full h-56 object-cover'
            />
            <div className="p-4">
              {/* Product Name */}
              <h2 className="font-semibold text-lg truncate">
                [オリメイク] 私は佐藤ではありません
              </h2>
              {/* Product Description */}
              <p className="text-sm text-gray-500 truncate">
                [オリメイク] 私は佐藤ではありません 苗字 tシャツ おもしろtシャツ 面白いtシャツ おもしろ メンズ 半袖 プレゼント パロディ
              </p>
              <div className="mt-2 flex items-center justify-between">
                {/* Product Price */}
                <span className='font-bold'>29,800￥</span>
                {/* Button of Adding To Cart */}
                <button className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700">
                  Add to cart
                </button>
              </div>
            </div>
          </div>

          {/* Product */}
          <div className="bg-white rounded shadow overflow-hidden">
            {/* Product Image */}
            <img 
              src="https://m.media-amazon.com/images/I/61Ima+Z8XZL._AC_SL1500_.jpg" 
              alt="Apple AirPods Pro 2" 
              className='w-full h-56 object-cover'
            />
            <div className="p-4">
              {/* Product Name */}
              <h2 className="font-semibold text-lg truncate">
                Apple AirPods Pro 2
              </h2>
              {/* Product Description */}
              <p className="text-sm text-gray-500 truncate">
                Apple AirPods Pro 2 ワイヤレスイヤホン、Bluetooth5.3、アクティブノイズキャンセリング、外部音取り込み、パーソナライズされた空間オーディオ、原音に忠実なサウンド、H2 チップ、USB-C 充電、防塵性能と耐汗耐水性能、「探す」対応、Qi充電、ヒアリング補助機能
              </p>
              <div className="mt-2 flex items-center justify-between">
                {/* Product Price */}
                <span className='font-bold'>29,800￥</span>
                {/* Button of Adding To Cart */}
                <button className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700">
                  Add to cart
                </button>
              </div>
            </div>
          </div>

          {/* Product */}
          <div className="bg-white rounded shadow overflow-hidden">
            {/* Product Image */}
            <img 
              src="https://m.media-amazon.com/images/I/61Ima+Z8XZL._AC_SL1500_.jpg" 
              alt="Apple AirPods Pro 2" 
              className='w-full h-56 object-cover'
            />
            <div className="p-4">
              {/* Product Name */}
              <h2 className="font-semibold text-lg truncate">
                Apple AirPods Pro 2
              </h2>
              {/* Product Description */}
              <p className="text-sm text-gray-500 truncate">
                Apple AirPods Pro 2 ワイヤレスイヤホン、Bluetooth5.3、アクティブノイズキャンセリング、外部音取り込み、パーソナライズされた空間オーディオ、原音に忠実なサウンド、H2 チップ、USB-C 充電、防塵性能と耐汗耐水性能、「探す」対応、Qi充電、ヒアリング補助機能
              </p>
              <div className="mt-2 flex items-center justify-between">
                {/* Product Price */}
                <span className='font-bold'>29,800￥</span>
                {/* Button of Adding To Cart */}
                <button className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700">
                  Add to cart
                </button>
              </div>
            </div>
          </div>

          {/* Product */}
          <div className="bg-white rounded shadow overflow-hidden">
            {/* Product Image */}
            <img 
              src="https://m.media-amazon.com/images/I/61Ima+Z8XZL._AC_SL1500_.jpg" 
              alt="Apple AirPods Pro 2" 
              className='w-full h-56 object-cover'
            />
            <div className="p-4">
              {/* Product Name */}
              <h2 className="font-semibold text-lg truncate">
                Apple AirPods Pro 2
              </h2>
              {/* Product Description */}
              <p className="text-sm text-gray-500 truncate">
                Apple AirPods Pro 2 ワイヤレスイヤホン、Bluetooth5.3、アクティブノイズキャンセリング、外部音取り込み、パーソナライズされた空間オーディオ、原音に忠実なサウンド、H2 チップ、USB-C 充電、防塵性能と耐汗耐水性能、「探す」対応、Qi充電、ヒアリング補助機能
              </p>
              <div className="mt-2 flex items-center justify-between">
                {/* Product Price */}
                <span className='font-bold'>29,800￥</span>
                {/* Button of Adding To Cart */}
                <button className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700">
                  Add to cart
                </button>
              </div>
            </div>
          </div>

          {/* Product */}
          <div className="bg-white rounded shadow overflow-hidden">
            {/* Product Image */}
            <img 
              src="https://m.media-amazon.com/images/I/61Ima+Z8XZL._AC_SL1500_.jpg" 
              alt="Apple AirPods Pro 2" 
              className='w-full h-56 object-cover'
            />
            <div className="p-4">
              {/* Product Name */}
              <h2 className="font-semibold text-lg truncate">
                Apple AirPods Pro 2
              </h2>
              {/* Product Description */}
              <p className="text-sm text-gray-500 truncate">
                Apple AirPods Pro 2 ワイヤレスイヤホン、Bluetooth5.3、アクティブノイズキャンセリング、外部音取り込み、パーソナライズされた空間オーディオ、原音に忠実なサウンド、H2 チップ、USB-C 充電、防塵性能と耐汗耐水性能、「探す」対応、Qi充電、ヒアリング補助機能
              </p>
              <div className="mt-2 flex items-center justify-between">
                {/* Product Price */}
                <span className='font-bold'>29,800￥</span>
                {/* Button of Adding To Cart */}
                <button className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700">
                  Add to cart
                </button>
              </div>
            </div>
          </div>

          {/* Product */}
          <div className="bg-white rounded shadow overflow-hidden">
            {/* Product Image */}
            <img 
              src="https://m.media-amazon.com/images/I/61Ima+Z8XZL._AC_SL1500_.jpg" 
              alt="Apple AirPods Pro 2" 
              className='w-full h-56 object-cover'
            />
            <div className="p-4">
              {/* Product Name */}
              <h2 className="font-semibold text-lg truncate">
                Apple AirPods Pro 2
              </h2>
              {/* Product Description */}
              <p className="text-sm text-gray-500 truncate">
                Apple AirPods Pro 2 ワイヤレスイヤホン、Bluetooth5.3、アクティブノイズキャンセリング、外部音取り込み、パーソナライズされた空間オーディオ、原音に忠実なサウンド、H2 チップ、USB-C 充電、防塵性能と耐汗耐水性能、「探す」対応、Qi充電、ヒアリング補助機能
              </p>
              <div className="mt-2 flex items-center justify-between">
                {/* Product Price */}
                <span className='font-bold'>29,800￥</span>
                {/* Button of Adding To Cart */}
                <button className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700">
                  Add to cart
                </button>
              </div>
            </div>
          </div>

          {/* Product */}
          <div className="bg-white rounded shadow overflow-hidden">
            {/* Product Image */}
            <img 
              src="https://m.media-amazon.com/images/I/61Ima+Z8XZL._AC_SL1500_.jpg" 
              alt="Apple AirPods Pro 2" 
              className='w-full h-56 object-cover'
            />
            <div className="p-4">
              {/* Product Name */}
              <h2 className="font-semibold text-lg truncate">
                Apple AirPods Pro 2
              </h2>
              {/* Product Description */}
              <p className="text-sm text-gray-500 truncate">
                Apple AirPods Pro 2 ワイヤレスイヤホン、Bluetooth5.3、アクティブノイズキャンセリング、外部音取り込み、パーソナライズされた空間オーディオ、原音に忠実なサウンド、H2 チップ、USB-C 充電、防塵性能と耐汗耐水性能、「探す」対応、Qi充電、ヒアリング補助機能
              </p>
              <div className="mt-2 flex items-center justify-between">
                {/* Product Price */}
                <span className='font-bold'>29,800￥</span>
                {/* Button of Adding To Cart */}
                <button className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700">
                  Add to cart
                </button>
              </div>
            </div>
          </div>

        </section>
      </main>

      {/* Footer */}
      <footer className="bg-white p-4 shadow fixed bottom-0 w-full">
        <div className="container mx-auto grid grid cols-2 md:grid-cols-4 gap-4">
          <div>
            <h3 className="font-bold">About us</h3>
            <p className="text-sm">Information about CAL Market.</p>
          </div>
          <div>
            <h3 className="font-bold">Contacts</h3>
            <p className="text-sm">Contact details here.</p>
          </div>
          <div>
            <h3 className="font-bold">Our SNS</h3>
            <p className="text-sm">Social network links.</p>
          </div>
          <div>
            <h3 className="font-bold">Credits</h3>
            <p className="text-sm">Additional Information.</p>
          </div>
        </div>
      </footer>

      {/* Mobile Bottom Navigation */}
      <nav className="lg:hidden bg-white fixed bottom-0 w-full flex justify-around items-center py-2 shadow">
        <img src="https://cal.co.jp/wordpress/wp-content/themes/temp_calrenew/img/logo.svg" alt="Logo" className='h-6' />
        <button><Search /></button>
        <button><ShoppingCart /></button>
        <button><User /></button>
      </nav>
    </div>
  )
}