<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('cau_hinh_he_thong', function (Blueprint $table) {
            $table->id();
            $table->string('ten_cau_hinh'); // 'thue', 'phi_san_bay', 'ty_gia', 'tien_te'
            $table->text('gia_tri'); // có thể là số hoặc JSON
            $table->timestamps();

            $table->unique('ten_cau_hinh');
        });
    }

    public function down()
    {
        Schema::dropIfExists('cau_hinh_he_thong');
    }
};
