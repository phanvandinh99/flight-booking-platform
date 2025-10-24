<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('san_bay', function (Blueprint $table) {
            $table->id();
            $table->string('ma_san_bay', 10)->unique(); // SGN, HAN...
            $table->string('ten_san_bay');
            $table->string('thanh_pho');
            $table->string('quoc_gia');
            $table->timestamps();
        });
    }

    public function down()
    {
        Schema::dropIfExists('san_bay');
    }
};
