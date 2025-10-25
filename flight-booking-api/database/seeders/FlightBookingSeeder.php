<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\SanBay;
use App\Models\HangHangKhong;
use App\Models\NguoiDung;
use App\Models\TuyenBay;
use App\Models\MayBay;
use App\Models\ChuyenBay;
use Illuminate\Support\Facades\Hash;

class FlightBookingSeeder extends Seeder
{
    public function run()
    {
        // === 1. SÂN BAY VIỆT NAM ===
        $airports = [
            ['SGN', 'Sân bay Tân Sơn Nhất', 'Thành phố Hồ Chí Minh', 'TP. Hồ Chí Minh'],
            ['HAN', 'Sân bay Nội Bài', 'Hà Nội', 'Hà Nội'],
            ['DAD', 'Sân bay Đà Nẵng', 'Đà Nẵng', 'Đà Nẵng'],
            ['CXR', 'Sân bay Cam Ranh', 'Cam Ranh', 'Khánh Hòa'],
            ['HPN', 'Sân bay Cát Bi', 'Hải Phòng', 'Hải Phòng'],
            ['VCL', 'Sân bay Chu Lai', 'Chu Lai', 'Quảng Nam'],
            ['VCA', 'Sân bay Cần Thơ', 'Cần Thơ', 'Cần Thơ'],
            ['PQC', 'Sân bay Phú Quốc', 'Phú Quốc', 'Kiên Giang'],
            ['THD', 'Sân bay Thọ Xuân', 'Thanh Hóa', 'Thanh Hóa'],
            ['VCS', 'Sân bay Côn Đảo', 'Côn Đảo', 'Bà Rịa – Vũng Tàu'],
            ['UIH', 'Sân bay Phù Cát', 'Quy Nhơn', 'Bình Định'],
            ['VDO', 'Sân bay Vân Đồn', 'Vân Đồn', 'Quảng Ninh'],
            ['DLK', 'Sân bay Liên Khương', 'Đà Lạt', 'Lâm Đồng'],
            ['BMV', 'Sân bay Buôn Ma Thuột', 'Buôn Ma Thuột', 'Đắk Lắk'],
            ['PXU', 'Sân bay Pleiku', 'Pleiku', 'Gia Lai'],
            ['DIN', 'Sân bay Đồng Hới', 'Đồng Hới', 'Quảng Bình'],
            ['NHA', 'Sân bay Nha Trang (T90)', 'Nha Trang', 'Khánh Hòa'],
            ['CAH', 'Sân bay Cà Mau', 'Cà Mau', 'Cà Mau'],
            ['VKG', 'Sân bay Rạch Giá', 'Rạch Giá', 'Kiên Giang'],
        ];

        foreach ($airports as $ap) {
            SanBay::create([
                'ma_san_bay' => $ap[0],
                'ten_san_bay' => $ap[1],
                'thanh_pho' => $ap[2],
                'quoc_gia' => 'Việt Nam'
            ]);
        }

        // === 2. HÃNG HÀNG KHÔNG VIỆT NAM ===
        $airlines = [
            ['Vietnam Airlines', 'VN'],
            ['Vietjet Air', 'VJ'],
            ['Bamboo Airways', 'QH'],
            ['Pacific Airlines', 'BL'],
        ];

        $airlineIds = [];
        foreach ($airlines as $al) {
            $airline = HangHangKhong::create([
                'ten_hang' => $al[0],
                'ma_hang' => $al[1],
                'trang_thai' => 'hoat_dong'
            ]);
            $airlineIds[$al[1]] = $airline->id;
        }

        // === 3. NGƯỜI DÙNG MẪU ===
        NguoiDung::create([
            'ten_day_du' => 'Admin Hệ Thống',
            'email' => 'admin@gmail.com',
            'mat_khau' => Hash::make('Abc123'),
            'vai_tro' => 'admin'
        ]);

        NguoiDung::create([
            'ten_day_du' => 'Đại Diện Vietnam Airlines',
            'email' => 'vn@gmail.com',
            'mat_khau' => Hash::make('Abc123'),
            'vai_tro' => 'dai_dien_hang',
            'ma_hang_hang_khong' => $airlineIds['VN']
        ]);

        NguoiDung::create([
            'ten_day_du' => 'Khách Hàng Mẫu',
            'email' => 'customer@gmail.com',
            'mat_khau' => Hash::make('Abc123'),
            'vai_tro' => 'khach_hang'
        ]);

        // === 4. TUYẾN BAY NỘI ĐỊA PHỔ BIẾN ===
        $routes = [
            ['SGN', 'HAN'],
            ['HAN', 'SGN'],
            ['SGN', 'DAD'],
            ['DAD', 'SGN'],
            ['HAN', 'DAD'],
            ['DAD', 'HAN'],
            ['SGN', 'PQC'],
            ['PQC', 'SGN'],
            ['HAN', 'PQC'],
            ['PQC', 'HAN'],
            ['SGN', 'CXR'],
            ['CXR', 'SGN'],
            ['HAN', 'CXR'],
            ['CXR', 'HAN'],
            ['SGN', 'VDO'],
            ['VDO', 'SGN'],
            ['HAN', 'VDO'],
            ['VDO', 'HAN'],
            ['DAD', 'PQC'],
            ['PQC', 'DAD'],
        ];

        foreach ($routes as $route) {
            $origin = SanBay::where('ma_san_bay', $route[0])->first();
            $dest = SanBay::where('ma_san_bay', $route[1])->first();
            if ($origin && $dest) {
                TuyenBay::create([
                    'san_bay_di' => $origin->id,
                    'san_bay_den' => $dest->id,
                    'duoc_phe_duyet' => true
                ]);
            }
        }

        // === 5. MÁY BAY THEO HÃNG (dữ liệu thực tế ước lượng) ===
        $aircrafts = [
            // Vietnam Airlines
            ['ma_hang' => 'VN', 'loai' => 'Boeing 787-9', 'ghe' => 270],
            ['ma_hang' => 'VN', 'loai' => 'Airbus A321', 'ghe' => 180],
            // Vietjet Air
            ['ma_hang' => 'VJ', 'loai' => 'Airbus A321neo', 'ghe' => 230],
            ['ma_hang' => 'VJ', 'loai' => 'Airbus A320', 'ghe' => 180],
            // Bamboo Airways
            ['ma_hang' => 'QH', 'loai' => 'Airbus A321neo', 'ghe' => 220],
            ['ma_hang' => 'QH', 'loai' => 'Boeing 787-9', 'ghe' => 290],
            // Pacific Airlines
            ['ma_hang' => 'BL', 'loai' => 'Airbus A320', 'ghe' => 180],
        ];

        foreach ($aircrafts as $ac) {
            MayBay::create([
                'ma_hang_hang_khong' => $airlineIds[$ac['ma_hang']],
                'loai_may_bay' => $ac['loai'],
                'tong_so_ghe' => $ac['ghe'],
                'so_do_ghe' => json_encode([
                    'pho_thong' => ['1A', '1B', '1C', '1D', '1E', '1F', '2A', '2B'],
                    'thuong_gia' => $ac['ma_hang'] === 'VN' || $ac['ma_hang'] === 'QH'
                        ? ['10A', '10B', '10C'] : null
                ])
            ]);
        }

        // === 6. CHUYẾN BAY MẪU (3 ngày tới, mỗi tuyến 1 chuyến) ===
        $sampleFlights = [
            ['VN', 'VN123', 'SGN', 'HAN'],
            ['VJ', 'VJ456', 'SGN', 'HAN'],
            ['QH', 'QH201', 'HAN', 'DAD'],
            ['BL', 'BL601', 'SGN', 'PQC'],
            ['VN', 'VN890', 'DAD', 'PQC'],
        ];

        foreach ($sampleFlights as $flight) {
            $airlineId = $airlineIds[$flight[0]];
            $origin = SanBay::where('ma_san_bay', $flight[2])->first();
            $dest = SanBay::where('ma_san_bay', $flight[3])->first();
            $route = TuyenBay::where('san_bay_di', $origin->id)
                ->where('san_bay_den', $dest->id)->first();

            if ($route) {
                // Chọn máy bay ngẫu nhiên của hãng
                $aircraft = MayBay::where('ma_hang_hang_khong', $airlineId)->inRandomOrder()->first();

                for ($i = 1; $i <= 3; $i++) {
                    $departure = now()->addDays($i)->setTime(8 + $i, 0);
                    $arrival = $departure->copy()->addHours(2)->addMinutes(15);

                    ChuyenBay::create([
                        'ma_hang_hang_khong' => $airlineId,
                        'ma_may_bay' => $aircraft->id,
                        'ma_chuyen_bay' => $flight[1],
                        'ma_tuyen_bay' => $route->id,
                        'gio_khoi_hanh' => $departure,
                        'gio_ha_canh' => $arrival,
                        'tan_suat' => 'hang_ngay',
                        'trang_thai' => 'du_kien'
                    ]);
                }
            }
        }
    }
}
