<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class SanBay extends Model
{
    protected $table = 'san_bay';
    protected $fillable = [
        'ma_san_bay',
        'ten_san_bay',
        'thanh_pho',
        'quoc_gia'
    ];

    public function tuyen_bay_di()
    {
        return $this->hasMany(TuyenBay::class, 'san_bay_di');
    }

    public function tuyen_bay_den()
    {
        return $this->hasMany(TuyenBay::class, 'san_bay_den');
    }
}
